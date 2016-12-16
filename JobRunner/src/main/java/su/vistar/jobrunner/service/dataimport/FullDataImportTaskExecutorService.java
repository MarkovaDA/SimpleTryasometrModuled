package su.vistar.jobrunner.service.dataimport;

import com.google.common.collect.Sets;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alidi.horeca.common.security.CustomPasswordGenerator;
import ru.alidi.horeca.persistence.dao.CustomerDao;
import ru.alidi.horeca.persistence.dao.FrontGroupDao;
import ru.alidi.horeca.persistence.dao.FrontUserDao;
import ru.alidi.horeca.persistence.entity.CustomerEntity;
import ru.alidi.horeca.persistence.entity.security.front.FrontGroupEntity;
import ru.alidi.horeca.persistence.entity.security.front.FrontUserEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Выполяет обновление БД. Сравнивает все строки, как правило по checkfield, за исключением остатков и заказов
 *
 * Нужно проверять checkfield, оно же хешсумма. В нашей БД нужно хранить хэш от последнего обновления. Сами его не меняем.
 *
 * Created by Shevchenko Roman on 11.11.16.
 */
@Service
public class FullDataImportTaskExecutorService {

    private static final Logger log = LoggerFactory.getLogger(FullDataImportTaskExecutorService.class);

    private static final String DEFAULT_CUSTOMER_GROUP = "customer";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^([a-zA-Z\\d][a-zA-Z\\d\\u002E\\u005F-]+?)@([a-zA-Z\\d][a-zA-Z-]*?\\u002E)+?([a-z]{2,}?)$");

    @Autowired
    private DatabaseUpdaterService updaterService;
    @Autowired
    private FrontUserDao frontUserDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private FrontGroupDao frontGroupDao;

    @Autowired
    @Qualifier("frontPasswordEncoder")
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomPasswordGenerator passwordGenerator;

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void executeUpdate(){

        log.info("Выполняем полное обновление внешних данных");

        final long time = System.currentTimeMillis();

        this.updaterService.updatePayTerms();
        this.updaterService.updateCategories();
        List<Long> updatedCustomers = this.updaterService.updateCustomers();
        this.updaterService.updateCustomerPayers();
        this.updaterService.updateCustomerDeliveryPoints();

        // новые пользователи Клиент
        this.createNewFrontUsers(updatedCustomers);

        //TODO: новые пользователи Оператор

        this.updaterService.updateCities();

        this.updaterService.updateItems();
        this.updaterService.updateRests();
        this.updaterService.updateOrders();

        //TODO: обновление эластика и рассылка почты по завершению транзакциии. не обновлять эластик при полном импорте,
        // т.к. слишком много данных. только при инкрементальном
        //рассылка будет ок, т.к. пишется в БД, а эластик не ок. посмотреть по поводу транзакций(??) в нем.

        log.info("полное обновление выполнено за {} мс", System.currentTimeMillis() - time);

    }


    protected void createNewFrontUsers(List<Long> customerIds){

        final FrontGroupEntity defaultGroup = frontGroupDao.getByName(DEFAULT_CUSTOMER_GROUP);

        this.customerDao.findCustomersWOFrontUser(customerIds)
                .forEach(c  -> this.createUser(c, defaultGroup));

    }

    protected void createUser(CustomerEntity customer, FrontGroupEntity defaultGroup){

        if (Strings.isNullOrEmpty(customer.getExtUsername())) {
            log.warn("Имя клиента {}({}) пустое, пользователь не будет создан", customer.getId(), customer.getExtId());
            return;
        }

        if (!EMAIL_PATTERN.matcher(customer.getExtUsername()).matches()) {
            log.warn("Имя клиента id:{}({}), username:{} не является корректным email, пользователь не будет создан", customer.getId(), customer.getExtId(), customer.getExtUsername());
            return;
        }

        FrontUserEntity user = new FrontUserEntity();
        user.setUsername(customer.getExtUsername());

        user.setGroups(Sets.newHashSet(defaultGroup));
        user.setCustomerEntity(customer);
        user.setRegistered(OffsetDateTime.now());
        user.setEnabled(customer.isActive());
        user.setDeleted(false);

        final String password = passwordGenerator.generate();
        final String encoded = passwordEncoder.encode(password + user.getUsername());

        user.setPassword(encoded);

        //TODO: send password

        frontUserDao.save(user);

    }
}
