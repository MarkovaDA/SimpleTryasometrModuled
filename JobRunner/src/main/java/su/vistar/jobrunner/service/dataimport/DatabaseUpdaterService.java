package ru.alidi.horeca.jobrunner.service.dataimport;

import com.google.common.base.Strings;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.alidi.horeca.common.text.Transliterator;
import ru.alidi.horeca.persistence.dao.*;
import ru.alidi.horeca.persistence.entity.*;
import ru.alidi.horeca.persistence.entity.embeddable.ExternalInfoEmbeddable;
import ru.alidi.horeca.persistence.entity.embeddable.ExternalOrderedInfoEmbeddable;
import ru.alidi.horeca.persistence.external.entity.*;
import ru.alidi.horeca.persistence.external.entity.embedded.EmbeddableCommonInfo;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.UUID.fromString;

@Service
public class DatabaseUpdaterService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUpdaterService.class);

    private ExternalDatabaseUpdaterService externalDatabaseUpdaterService;

    @Autowired
    public DatabaseUpdaterService(
            ExternalDatabaseUpdaterService externalDatabaseUpdaterService) {

        this.externalDatabaseUpdaterService = externalDatabaseUpdaterService;
    }

    public void updateCategories() {

        CommonUpdateTemplate<ExternalCategoryEntity, CategoryEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalCategoryEntity, CategoryEntity>("Категория", CategoryEntity.class) {

            @Override
            List<ExternalCategoryEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstCategories();
            }

            @Override
            List<ExternalCategoryEntity> fetchNextPart(ExternalCategoryEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchCategories(last.getExtId(), limit);
            }

            @Override
            protected void afterUpdate() {
                DatabaseUpdaterService.this.categoryDao.linkCategories();
            }

            @Override
            public CategoryEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .categoryDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalCategoryEntity external, CategoryEntity internal) {
                return !Objects.equals(
                        internal.getExternalInfo().getCheckfield(),
                        external.getCommonInfo().getCheckfield()
                );
            }

            @Override
            void saveInternal(CategoryEntity categoryEntity) {
                DatabaseUpdaterService.this
                        .categoryDao.save(categoryEntity);

            }

            @Override
            void updateInternal(ExternalCategoryEntity extCat, CategoryEntity intCat) {
                ExternalOrderedInfoEmbeddable externalInfo = intCat.getExternalInfo();
                EmbeddableCommonInfo commonInfo = extCat.getCommonInfo();

                //common info
                externalInfo.setActive(commonInfo.getActive());
                externalInfo.setCheckfield(commonInfo.getCheckfield());
                externalInfo.setOrderfield(commonInfo.getOrderfield());
                externalInfo.setExtId(UUID.fromString(extCat.getExtId()));

                //category specific
                intCat.setExtParentId(UUID.fromString(extCat.getExtParentId()));
                String trimmedName = StringUtils.trimWhitespace(extCat.getName());
                intCat.setName(trimmedName);
                intCat.setDescription(extCat.getDescription());

                if ( Strings.isNullOrEmpty(intCat.getUrl()) ) {

                    //урл должны быть уникальны
                    final String suffix = Integer.toString(ThreadLocalRandom.current().nextInt(100000));
                    intCat.setUrl(Transliterator.transliterate(trimmedName) + suffix);
                }
            }

        };

        updateTemplate.updateAll();

    }

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private CategoryDao categoryDao;

    public void updateItems() {

        CommonUpdateTemplate<ExternalItemEntity, ItemEntity> updateTemplate = new CommonUpdateTemplate<ExternalItemEntity, ItemEntity>("Товар", ItemEntity.class,  1000) {

            private Map<String, ItemMeasureEntity> measureEntityMap = new TreeMap<>();
            private Map<UUID, Long> categoryMap = new TreeMap<>();

            @Override
            protected void beforeUpdate() {
                DatabaseUpdaterService.this.categoryDao.listAll().stream()
                        .filter(c -> c.getExternalInfo().getExtId() != null)
                        .forEach(c -> this.categoryMap.put(c.getExternalInfo().getExtId(), c.getId()));

                log.info("Загруженно категорий {}", this.categoryMap.size());
            }

            @Override
            protected void afterUpdate() {
                this.measureEntityMap = null;
                this.categoryMap = null;
            }


            @Override
            List<ExternalItemEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstItems(limit);
            }

            @Override
            List<ExternalItemEntity> fetchNextPart(ExternalItemEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchItems(last.getExtId(), limit);
            }


            @Override
            public ItemEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .itemDao.getByExtIdWOCacheL1(uuid);
            }

            @Override
            boolean isModified(ExternalItemEntity external, ItemEntity internal) {
                return !Objects.equals(
                        internal.getExternalInfo().getCheckfield(),
                        external.getCommonInfo().getCheckfield()
                );
            }

            @Override
            void updateInternal(ExternalItemEntity externalItemEntity, ItemEntity itemEntity) {
                updateItemEntity(externalItemEntity, itemEntity);
            }

            @Override
            void saveInternal(ItemEntity internal) {
                DatabaseUpdaterService.this
                        .itemDao.saveAndFlush(internal);
            }

            private void updateItemEntity(ExternalItemEntity extItem, ItemEntity intItem){

                ExternalOrderedInfoEmbeddable externalInfo = intItem.getExternalInfo();
                EmbeddableCommonInfo commonInfo = extItem.getCommonInfo();

                //common info
                externalInfo.setExtId(UUID.fromString(extItem.getExtId()));
                externalInfo.setActive(commonInfo.getActive());
                externalInfo.setCheckfield(commonInfo.getCheckfield());
                externalInfo.setOrderfield(commonInfo.getOrderfield());


                String trimmedName = StringUtils.trimWhitespace(extItem.getName());
                //item specific
                if ( Strings.isNullOrEmpty(intItem.getUrl()) ) {

                    final String suffix = Integer.toString(ThreadLocalRandom.current().nextInt(1000000));
                    intItem.setUrl(Transliterator.transliterate(trimmedName) + suffix);
                }

                intItem.setName(trimmedName);
                if (!Strings.isNullOrEmpty(extItem.getExtCategoryId())){

                    UUID catUuid = UUID.fromString(extItem.getExtCategoryId());
                    intItem.setExtCategoryId(catUuid);

                    Long id = categoryMap.get(catUuid);
                    if (id != null) {
                        intItem.setCategory(DatabaseUpdaterService.this
                                .categoryDao.load(id));
                    }
                }

                intItem.setItemMeasure(getMeasureEntity(extItem.getMeasureId()));

                intItem.setEanCode(extItem.getEanCode());
                intItem.setManufacturer(extItem.getManufactureId());
                intItem.setDirectionCode(extItem.getDirectionCode());
                Date now = new Date();
                intItem.setCreateDate(now);
                intItem.setUpdateDate(now);
                intItem.setBlocked(false);

            }

            private ItemMeasureEntity getMeasureEntity(String code){
                if (code == null) return null;

                ItemMeasureEntity measure;
                if (!this.measureEntityMap.containsKey(code)) {

                    measure = DatabaseUpdaterService.this
                            .itemMeasureDao.getByCode(code);

                    if (measure == null) {
                        measure = new ItemMeasureEntity();
                        measure.setCode(code);
                        measure.setName(code);
                        measure.setShortName(code);
                        measure.setShortNameEng(code);

                        DatabaseUpdaterService.this
                                .itemMeasureDao.save(measure);
                    }
                    this.measureEntityMap.put(code, measure);
                } else {
                    measure = this.measureEntityMap.get(code);
                }

                return measure;
            }

        };

        updateTemplate.updateAll();

    }

    public List<Long> updateCustomers() {

        CommonUpdateTemplate<ExternalCustomerEntity, CustomerEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalCustomerEntity, CustomerEntity>("Клиент", CustomerEntity.class) {

            @Override
            List<ExternalCustomerEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstCustomers();
            }

            @Override
            List<ExternalCustomerEntity> fetchNextPart(ExternalCustomerEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchCustomers(last.getExtId(), limit);
            }

            @Override
            public CustomerEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .customerDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalCustomerEntity externalCustomerEntity, CustomerEntity customerEntity) {
                return !Objects.equals(customerEntity.getCheckfield(), externalCustomerEntity.getCheckfield());
            }

            @Override
            void updateInternal(ExternalCustomerEntity extCustomer, CustomerEntity intCustomer) {
                intCustomer.setActive(extCustomer.getActive());
                intCustomer.setCheckfield(extCustomer.getCheckfield());
                intCustomer.setExtId(UUID.fromString(extCustomer.getExtId()));
                intCustomer.setName(extCustomer.getName());

                if (StringUtils.hasText(extCustomer.getUsername())){
                    intCustomer.setExtUsername(StringUtils.trimWhitespace(extCustomer.getUsername()));
                }
            }

            @Override
            void saveInternal(CustomerEntity customerEntity) {
                DatabaseUpdaterService.this
                        .customerDao.save(customerEntity);
            }
        };

        return updateTemplate.updateAll();
    }

    public void updateCustomerPayers() {

        CommonUpdateTemplate<ExternalCustomerPayerEntity, PayerEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalCustomerPayerEntity, PayerEntity>("Плательщик", PayerEntity.class) {

            @Override
            List<ExternalCustomerPayerEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchFirstCustomerPayers();
            }

            @Override
            List<ExternalCustomerPayerEntity> fetchNextPart(ExternalCustomerPayerEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchCustomerPayers(last.getExtId(), limit);
            }

            @Override
            public PayerEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .payerDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalCustomerPayerEntity ext, PayerEntity internalPayer) {
                return !Objects.equals(internalPayer.getExternalInfo().getCheckfield(), ext.getCheckfield());
            }

            @Override
            void updateInternal(ExternalCustomerPayerEntity extPayer, PayerEntity intPayer) {
                //common info
                ExternalInfoEmbeddable externalInfo = intPayer.getExternalInfo();
                externalInfo.setExtId(UUID.fromString(extPayer.getExtId()));
                externalInfo.setCheckfield(extPayer.getCheckfield());
                externalInfo.setActive(extPayer.getActive());

                intPayer.setCustomer(
                        DatabaseUpdaterService.this
                                .customerDao.getByExtId(UUID.fromString(extPayer.getCustomerId()))
                );
                intPayer.setName(extPayer.getName());
                intPayer.setDirectionCode(extPayer.getDirectionCode());
                intPayer.setInn(extPayer.getInn());
                intPayer.setKpp(extPayer.getKpp());

                intPayer.setPrepayed(extPayer.getQtyOrdertoprepayment());
                intPayer.setBarcodePage(extPayer.getQtySpecification());
                intPayer.setTorg12(extPayer.getQtyTorg12());
                intPayer.setInvoice(extPayer.getQtyInvoice());
                intPayer.setCertifications(extPayer.getQtyCertificate());
                intPayer.setUpd(extPayer.getQtyUpd());
            }

            @Override
            void saveInternal(PayerEntity payerEntity) {
                DatabaseUpdaterService.this
                    .payerDao.save(payerEntity);
            }
        };

        updateTemplate.updateAll();

    }

    public void updateCustomerDeliveryPoints() {

        CommonUpdateTemplate<ExternalDeliveryPointEntity, DeliveryPointEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalDeliveryPointEntity, DeliveryPointEntity>("Точка доставки", DeliveryPointEntity.class) {

            @Override
            List<ExternalDeliveryPointEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstDeliveryPoints();
            }


            @Override
            List<ExternalDeliveryPointEntity> fetchNextPart(ExternalDeliveryPointEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchDeliveryPoints(last.getExtId(), limit);
            }

            @Override
            public DeliveryPointEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .deliveryPointDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalDeliveryPointEntity ext, DeliveryPointEntity internalDeliveryPoint) {
                return !Objects.equals(internalDeliveryPoint.getCheckfield(), ext.getCheckfield());
            }

            @Override
            void updateInternal(ExternalDeliveryPointEntity extPoint, DeliveryPointEntity intPoint) {
                //common info
                intPoint.setExtId(UUID.fromString(extPoint.getExtId()));
                intPoint.setCheckfield(extPoint.getCheckfield());
                intPoint.setActive(extPoint.getActive());

                intPoint.setPayer(
                        DatabaseUpdaterService.this
                        .payerDao.getByExtId(UUID.fromString(extPoint.getCustomerPayerId()))
                );
                intPoint.setAddress(extPoint.getRawAdress());
                intPoint.setName(extPoint.getName());
                intPoint.setDirectionCode(extPoint.getDirectionCode());
                intPoint.setInn(extPoint.getInn());
                intPoint.setKpp(extPoint.getKpp());
                intPoint.setKladrAddress(extPoint.getKladrAdress());

                OffsetDateTime now = OffsetDateTime.now();
                intPoint.setUpdateDate(now);
                intPoint.setCreateDate(now);
            }

            @Override
            void saveInternal(DeliveryPointEntity deliveryPointEntity) {
                DatabaseUpdaterService.this
                        .payerDao.save(deliveryPointEntity);
            }

        };

        updateTemplate.updateAll();

    }

    public void updatePayTerms() {

        CommonUpdateTemplate<ExternalPayTermEntity, PayTermEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalPayTermEntity, PayTermEntity>("условия оплаты", PayTermEntity.class) {
            @Override
            List<ExternalPayTermEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchFirstPayTerms();
            }

            @Override
            List<ExternalPayTermEntity> fetchNextPart(ExternalPayTermEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchPayTerms(last.getExtId(), limit);
            }

            @Override
            public PayTermEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this
                        .payTermDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalPayTermEntity external, PayTermEntity internal) {
                return !Objects.equals(internal.getCheckfield(), external.getCheckfield());
            }

            @Override
            void updateInternal(ExternalPayTermEntity extPayTerm, PayTermEntity intPayTerm) {
                //common info
                intPayTerm.setCheckfield(extPayTerm.getCheckfield());
                intPayTerm.setExtId(UUID.fromString(extPayTerm.getExtId()));
                intPayTerm.setName(extPayTerm.getName());
            }

            @Override
            void saveInternal(PayTermEntity payTermEntity) {
                DatabaseUpdaterService.this
                        .payTermDao.save(payTermEntity);
            }
        };

        updateTemplate.updateAll();
    }

    @Autowired
    private AdminUnitDao adminUnitDao;

    public void updateCities() {

        CommonUpdateTemplate<ExternalCityEntity, AdminUnitEntity> updateTemplate
                = new CommonUpdateTemplate<ExternalCityEntity, AdminUnitEntity>("Город", AdminUnitEntity.class) {

            @Override
            public AdminUnitEntity getInternalEntity(UUID uuid) {
                return DatabaseUpdaterService.this.adminUnitDao.getByExtId(uuid);
            }

            @Override
            boolean isModified(ExternalCityEntity external, AdminUnitEntity adminUnitEntity) {
                return !Objects.equals(external.getCheckfield(), adminUnitEntity.getExternalInfo().getCheckfield());
            }

            @Override
            void updateInternal(ExternalCityEntity external, AdminUnitEntity internal) {
                ExternalInfoEmbeddable externalInfo = internal.getExternalInfo();

                externalInfo.setActive(external.getActive());
                externalInfo.setCheckfield(external.getCheckfield());
                externalInfo.setExtId(UUID.fromString(external.getExtId()));

                String name = StringUtils.trimWhitespace(external.getName());
                internal.setName(name);
                internal.setAdmUnitShort(name);
                String transliterated = Transliterator.transliterate(name);
                internal.setUrl(transliterated);
            }

            @Override
            void saveInternal(AdminUnitEntity adminUnitEntity) {
                DatabaseUpdaterService.this.adminUnitDao.save(adminUnitEntity);
            }

            @Override
            List<ExternalCityEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstCities();
            }

            @Override
            List<ExternalCityEntity> fetchNextPart(ExternalCityEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchCities(last.getExtId(), limit);
            }
        };

        updateTemplate.updateAll();
    }

    @Autowired
    private BranchDao branchDao;
/*
    public void updateBranches() {
        CommonUpdateTemplate<ExternalBranchEntity, BranchEntity> updateTemplate =
                new CommonUpdateTemplate<ExternalBranchEntity, BranchEntity>("Департамент", BranchEntity.class) {
                    @Override
                    public BranchEntity getInternalEntity(UUID uuid) {
                        return DatabaseUpdaterService.this.branchDao.getByExtId(uuid);
                    }

                    @Override
                    boolean isModified(ExternalBranchEntity external, BranchEntity internal) {
                        return !Objects.equals(external.getCheckfield(), internal.getExternalInfo().getCheckfield());
                    }

                    @Override
                    void updateInternal(ExternalBranchEntity external, BranchEntity internal) {
                        ExternalInfoEmbeddable externalInfo = internal.getExternalInfo();
                        externalInfo.setExtId(fromString(external.getExtId()));
                        externalInfo.setCheckfield(external.getCheckfield());
                        externalInfo.setActive(external.getActive());

                        internal.setName(external.getName());
                        internal.setDirectionCode(external.getDirectionCode());
                        internal.setExtCityId(fromString(external.getExtCityId()));

                    }

                    @Override
                    protected void afterUpdate() {
                        int affected = DatabaseUpdaterService.this.branchDao.linkAdminUnitsForAll();
                        log.info("Обновлено {} отделений", affected);
                    }

                    @Override
                    void saveInternal(BranchEntity branchEntity) {
                        DatabaseUpdaterService.this.branchDao.save(branchEntity);
                    }

                    @Override
                    List<ExternalBranchEntity> fetchFirstData(int limit) {
                        return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstBranches();
                    }

                    @Override
                    List<ExternalBranchEntity> fetchNextPart(ExternalBranchEntity last, int limit, int offset) {
                        return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchBranches(last.getExtId(), limit);
                    }
                };

        updateTemplate.updateAll();
    }
*/
    @Autowired
    private WarehouseDao warehouseDao;

/*
    public void updateWarehouses() {
        CommonUpdateTemplate<ExternalWarehouseEntity, WarehouseEntity> updateTemplate =
                new CommonUpdateTemplate<ExternalWarehouseEntity, WarehouseEntity>("Склад", WarehouseEntity.class) {
                    @Override
                    public WarehouseEntity getInternalEntity(UUID uuid) {
                        return DatabaseUpdaterService.this.warehouseDao.getByExtId(uuid);
                    }

                    @Override
                    boolean isModified(ExternalWarehouseEntity external, WarehouseEntity internal) {
                        return !Objects.equals(external.getCheckfield(), internal.getExternalInfo().getCheckfield());
                    }

                    @Override
                    void updateInternal(ExternalWarehouseEntity external, WarehouseEntity internal) {


                        ExternalInfoEmbeddable externalInfo = internal.getExternalInfo();

                        externalInfo.setActive(external.getActive());
                        externalInfo.setCheckfield(external.getCheckfield());
                        externalInfo.setExtId(fromString(external.getExtId()));

                        internal.setName(external.getName());
                        internal.setExtBranchId(fromString(external.getExtBranchId()));

                    }

                    @Override
                    void saveInternal(WarehouseEntity warehouseEntity) {
                        DatabaseUpdaterService.this.warehouseDao.save(warehouseEntity);
                    }

                    @Override
                    protected void afterUpdate() {
                        int i = DatabaseUpdaterService.this.warehouseDao.linkBranchesForAll();
                        log.info("Обновлено {} связей склад - департамент", i);
                    }

                    @Override
                    List<ExternalWarehouseEntity> fetchFirstData(int limit) {
                        return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchFirstWarehouses();
                    }

                    @Override
                    List<ExternalWarehouseEntity> fetchNextPart(ExternalWarehouseEntity last, int limit, int offset) {
                        return DatabaseUpdaterService.this.externalDatabaseUpdaterService.fetchWarehouses(last.getExtId(), limit);
                    }
                };

        updateTemplate.updateAll();
    }
*/

    @Autowired
    private RestDao restDao;

    public void updateRests(){
        UpdateTemplate<ExternalRestEntity, RestEntity> updateTemplate
                = new UpdateTemplate<ExternalRestEntity, RestEntity>("Остатки") {


            @Override
            protected void beforeUpdate() {
                restDao.deleteAll();
            }

            @Override
            Long updateData(ExternalRestEntity external) {

                RestEntity entity = createRestEntity(external);
                if (entity != null){
                    restDao.save(entity);
                    return entity.getRestId().getItem().getId();
                }

                return null;
            }

            private RestEntity createRestEntity(ExternalRestEntity r) {
                AdminUnitEntity adminUnit = adminUnitDao.getByExtId(
                        fromString(r.getRestId().getCityId())
                );
                if (adminUnit == null){
                    log.warn("Невозможно обновить остатки с ключем city_id:{} item_id:{}, в БД нет указанного города",
                            r.getRestId().getCityId(), r.getRestId().getItemId());
                    return null;
                }

                ItemEntity item = itemDao.getByExtId(
                        fromString(r.getRestId().getItemId())
                );
                if (item == null){
                    log.warn("Невозможно обновить остатки с ключем city_id:{} item_id:{}, в БД нет указанного товара",
                            r.getRestId().getCityId(), r.getRestId().getItemId());
                    return null;
                }

                RestEntity rest = new RestEntity(
                        adminUnit,
                        item,
                        (double) r.getQuantity(),
                        fromString(r.getRestId().getCityId()),
                        fromString(r.getRestId().getItemId())
                );

                return rest;
            }

            @Override
            List<ExternalRestEntity> fetchFirstData(int limit) {

                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchRests(0, limit);
            }

            @Override
            List<ExternalRestEntity> fetchNextPart(ExternalRestEntity last, int limit, int offset) {

                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchRests(offset, limit);
            }
        };

        updateTemplate.updateAll();
    }


    @Autowired
    private PayTermDao payTermDao;

    @Autowired
    private DeliveryPointDao deliveryPointDao;

    @Autowired
    private PayerDao payerDao;

    @Autowired
    private OrdersDao ordersDao;

    @Autowired
    private OrderHeaderDao orderHeaderDao;

    @Autowired
    private ItemMeasureDao itemMeasureDao;

    @Autowired
    private OrderHeaderGenerationDao orderHeaderGenerationDao;

    @Autowired
    private CustomerDao customerDao;

    public void updateOrders(){

        UpdateTemplate<ExternalOrderHeaderEntity, OrderHeaderEntity> updateTemplate
                = new UpdateTemplate<ExternalOrderHeaderEntity, OrderHeaderEntity>("Заказы") {
            @Override
            Long updateData(ExternalOrderHeaderEntity external) {

                OrderHeaderEntity internal = DatabaseUpdaterService.this
                        .orderHeaderDao.getByExtId(external.getExtId());

                {
                    //пробуем получить актуальный хидер по док_код на случай если возикла ошибка в структуре внешней БД
                    OrderHeaderEntity oldOh = DatabaseUpdaterService.this
                            .orderHeaderDao.getLastGenByDocCode(external.getDocCode());

                    if (oldOh != null && !Objects.equals(oldOh.getExtId(), external.getExtId())) {
                        log.warn("Обнаружено дублирование заказа по doc_code, будет проигнорирован, id:{}, doc_code:{}",
                                external.getExtId(), external.getDocCode());

                        return null;
                    }
                }


                Long oheId = null;
                boolean isNew = internal == null;

                if (!isNew) {
                    Hibernate.initialize(internal.getLines());
                }

                if (isNew || isNeedUpdate(external, internal)) {

                    final OrdersEntity orders;
                    final OrderHeaderGenerationEntity ohge;
                    if (isNew) {

                        orders = new OrdersEntity();
                        orders.setCustomer(
                                DatabaseUpdaterService.this
                                        .customerDao.getByPayerExtId(fromString(external.getExtCustomerPayerId()))
                        );
                        orders.setCreateDate(external.getDocDate());

                        DatabaseUpdaterService.this
                                .ordersDao.save(orders);

                        ohge = new OrderHeaderGenerationEntity();
                        ohge.setDocCode(external.getDocCode());
                        ohge.setOrderId(orders.getId());
                        ohge.setGeneration(0);

                    } else {

                        ohge = orderHeaderGenerationDao.getById(external.getDocCode());
                        orders = ordersDao.load(ohge.getOrderId());
                        ohge.setGeneration(ohge.getGeneration() + 1);

                    }

                    OrderHeaderEntity ohe = this.createNewGeneration(external, ohge.getGeneration());
                    DatabaseUpdaterService.this
                            .orderHeaderDao.save(ohe);

                    this.createLines(external, ohe);

                    ohe.setOrder(orders);

                    DatabaseUpdaterService.this
                            .orderHeaderDao.save(ohe);

                    oheId = ohe.getId();

                    ohge.setOrderHeaderId(oheId);

                    DatabaseUpdaterService.this
                            .orderHeaderGenerationDao.save(ohge);

                }


                return oheId;
            }

            private OrderHeaderEntity createNewGeneration(ExternalOrderHeaderEntity external,
                                                          Integer generation) {
                OrderHeaderEntity ohe = new OrderHeaderEntity();

                ohe.setExtId(external.getExtId());
                ohe.setActive(external.getActive());
                ohe.setCheckfield(external.getCheckfield());
                ohe.setExtCustomerPayerId(fromString(external.getExtCustomerPayerId()));
                ohe.setExtDeliveryPointId(fromString(external.getExtDeliveryPointId()));

                ohe.setGeneration(generation);

                ohe.setDocCode(external.getDocCode());
                ohe.setDocumentNum(external.getDocumentNum());
                ohe.setDocDate(external.getDocDate());
                ohe.setShipmentDate(external.getShipmentDate());

                ohe.setDeliveryPoint(DatabaseUpdaterService.this
                        .deliveryPointDao.getByExtId(fromString(external.getExtDeliveryPointId())));
                ohe.setPayer(DatabaseUpdaterService.this
                        .payerDao.getByExtId(fromString(external.getExtCustomerPayerId())));
                ohe.setPayTerm(DatabaseUpdaterService.this
                        .payTermDao.getByExtId(fromString(external.getPayTermId())));

                return ohe;
            }

            private void createLines(ExternalOrderHeaderEntity external, OrderHeaderEntity ohe) {
                Collection<OrderLineEntity> lines = new ArrayList<>(external.getLines().size());
                for (ExternalOrderLineEntity extLine : external.getLines()){

                    OrderLineEntity line = new OrderLineEntity();
                    line.getId().setLineNumber(extLine.getOrderLineId().getLineNumber());
                    line.getId().setOrderHeaderId(ohe.getId());

                    //external info
                    line.setCheckfield(extLine.getCheckfield());
                    line.setExtItemId(fromString(extLine.getExtItemId()));
                    line.setExtOrderHeaderId(extLine.getOrderLineId().getExtOrderId());
                    line.setExtMeasureId(extLine.getItemMeasureId());

                    line.setCurrencyCode(extLine.getCurrencyCode());
                    line.setQtyCancelled(Double.valueOf(extLine.getCancelledQty()));
                    line.setQtyOrdered(Double.valueOf(extLine.getOrderedQty()));
                    line.setQtyShipped(Double.valueOf(extLine.getShippedQty()));
                    line.setQtySystemPlaced(Double.valueOf(extLine.getSystemPlacedQty()));
                    line.setShippedPrice(Double.valueOf(extLine.getShippedPrice()));

                    line.setItem(DatabaseUpdaterService.this
                            .itemDao.getByExtId(fromString(extLine.getExtItemId())));
                    line.setItemMeasure(DatabaseUpdaterService.this
                            .itemMeasureDao.getByCode(extLine.getItemMeasureId()));

                    lines.add(line);
                }

                ohe.setLines(lines);
            }

            /**
             * Сравниваем качественный и количественный состав внешнего заказа
             * @param external
             * @param internal
             * @return
             */
            private boolean isNeedUpdate(ExternalOrderHeaderEntity external, OrderHeaderEntity internal) {

                boolean newRecord = internal == null;

                boolean changed = false;
                if (!newRecord){

                    if (!Objects.equals(external.getCheckfield(), internal.getCheckfield())) {

                        return true;

                    }

                    if (internal.getLines().size() != external.getLines().size()) {

                        return true;

                    }

                    Iterator<OrderLineEntity> intIt = internal.getLines().iterator();
                    Iterator<ExternalOrderLineEntity> extIt = external.getLines().iterator();
                    while (intIt.hasNext() && extIt.hasNext()) {

                        OrderLineEntity intLine = intIt.next();
                        ExternalOrderLineEntity extLine = extIt.next();

                        if (!Objects.equals(
                                intLine.getCheckfield(),
                                extLine.getCheckfield()
                        ) || !Objects.equals(
                                intLine.getId().getLineNumber(),
                                extLine.getOrderLineId().getLineNumber()
                        )) {

                            return true;

                        }

                    }

                }

                return newRecord || changed;
            }

            @Override
            List<ExternalOrderHeaderEntity> fetchFirstData(int limit) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchFirstOrders();
            }

            @Override
            List<ExternalOrderHeaderEntity> fetchNextPart(ExternalOrderHeaderEntity last, int limit, int offset) {
                return DatabaseUpdaterService.this
                        .externalDatabaseUpdaterService.fetchOrdersInitialized(last.getExtId(), limit);
            }
        };

        updateTemplate.updateAll();

    }


    /**
     * Упрощенный вариант шаблона, подходит для внешних сущностей имплементирующих
     * ExternalEntity и внутренних HasLongId
     *
     * @param <E> тип внешней сущности
     * @param <I> тип внутренней сущности
     *
     * @see ExternalEntity
     * @see HasLongId
     * @see UpdateTemplate
     */
    public abstract static class CommonUpdateTemplate<E extends ExternalEntity, I extends HasLongId>
            extends UpdateTemplate<E, I> {

        private final Class<I> clazz;

        /**
         * Конструктор по-умолчанию
         *
         * @param name имя обновляемой сущности
         * @param internalClazz класс внутренней сущности
         */
        public CommonUpdateTemplate(String name, Class<I> internalClazz) {
            super(name);
            this.clazz = internalClazz;
        }

        /**
         * Конструктор с укзанием лимита, т.е. колва записей в одной итерации обновления
         *
         * @param name имя обновляемой сущности
         * @param internalClazz класс внутренней сущности
         * @param limit кол-во записей в одной итерации
         */
        public CommonUpdateTemplate(String name, Class<I> internalClazz, int limit) {
            super(name, limit);
            this.clazz = internalClazz;
        }

        @Override
        public Long updateData(E external) {

            log.debug("Начинаем обновлять {} : {}", name, external.getExtId());

            I internal = getInternalEntity(UUID.fromString(external.getExtId()));

            boolean newItem = internal == null;
            if (newItem) {
                log.debug("Новый {}, создаём: {}", name, external.getExtId());
                internal = newInternalEntity();
            }

            boolean needUpdate = newItem || isModified(external, internal);


            if (needUpdate) {
                log.debug("{} изменился, необходимо обновление: {}", name, external.getExtId());

                updateInternal(external, internal);

                saveInternal(internal);

                return internal.getId();
            } else {
                return null;
            }
        }

        public abstract I getInternalEntity(UUID uuid);

        /**
         * Проверяет сущности на изменения
         *
         * @param e внешняя сущность
         * @param i внутренняя
         * @return true если необходимо обновить
         */
        abstract boolean isModified(E e, I i);

        /**
         * Должен вернуть новую сущность
         *
         * @return новый объект внутренней сущности
         */
        I newInternalEntity() {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * здесь необходимо обновить внутреннюю сущность в соответствии с внешней
         *
         * @param e внешняя сущность
         * @param i внутренняя
         */
        abstract void updateInternal(E e, I i);

        /**
         * Должен сохранить в БД
         *
         * @param i внутренняя сущность
         */
        abstract void saveInternal(I i);

    }


    /**
     * Шаблон обновления данных из внешеней БД. Обновляет порциями. имплементация должна
     * возвращать порции данных в стиле "бесконечного скролла".
     *
     * @param <E> тип внешней сущности
     * @param <I> тип внутренней сущности
     */
    public abstract static class UpdateTemplate<E, I> {

        protected final String name;
        protected final int limit;

        /**
         * Конструктор по-умолчанию, кол-во записей в одной итерации 100
         *
         * @param name имя обновляемой сущности
         */
        public UpdateTemplate(String name) {
            this(name, 100);
        }

        /**
         * Конструктор по-умолчанию
         *
         * @param name имя обновляемой сущности
         * @param limit кол-во записей в одной итерации
         */
        public UpdateTemplate(String name, int limit) {
            this.name = name;
            this.limit = limit;
        }

        /**
         * Возвращает список id созданных или обновленных элементов
         *
         * @return список id созданных или обновленных элементов
         */
        public List<Long> updateAll() {

            log.info("Начинаем обновление {}", this.name);

            long startTime = System.currentTimeMillis();

            this.beforeUpdate();

            int offset = limit;
            final long fetchFirstTime = System.currentTimeMillis();
            List<E> externalData = fetchFirstData(limit);
            log.debug("Загружена первая часть данных {} за {} мс", name, System.currentTimeMillis() - fetchFirstTime);

            E last = null;

            List<Long> updatedIds = new LinkedList<>();
            while (externalData.size() > 0) {

                final long batchUpdateTime = System.currentTimeMillis();
                for (int i = 0; i < externalData.size(); i++) {
                    E externalItemEntity = externalData.get(i);

                    Long id = updateData(externalItemEntity);

                    if (id != null) {
                        updatedIds.add(id);
                    }

                    last = externalItemEntity;
                }

                log.info("Обработано {} записей типа {} за {} мс. Итого обновлено {} ...",
                        externalData.size(), this.name, System.currentTimeMillis() - batchUpdateTime, updatedIds.size());

                final long fetchTime = System.currentTimeMillis();
                externalData = fetchNextPart(last, limit, offset);
                log.debug("Загружена часть данных {}, типа {} за {} мс",
                        externalData.size(), name, System.currentTimeMillis() - fetchTime);

                offset += externalData.size();
            }

            this.afterUpdate();

            log.info("Обновление записей типа {} завершено. Обновлено записей {} за {} мс",
                    this.name, updatedIds.size(), System.currentTimeMillis() - startTime);

            return updatedIds;

        }

        /**
         * Выполняется перед обновления всех сущностей
         */
        protected void beforeUpdate() {   }
        /**
         * Выполняется после обновления всех сущностей
         */
        protected void afterUpdate() {   }

        /**
         * обновляет элемент внутренней БД соответствующий внешнему
         *
         * @param external элемент из внешней БД
         * @return идентификатор внутреннего элемента или null если элемент не нуждается в обновлении
         */
        abstract Long updateData(E external);

        /**
         * возвращает первую порцию данных
         *
         * @param limit ограничение по кол-ву строк в результирующей выборке
         * @return список элементов
         */
        abstract List<E> fetchFirstData(int limit);

        /**
         * возвращает n-ное кол-во элементов
         * <p> варианты имплементации : <br/>
         * * начиная с lastId, данные в запросе отсортированы по id <br/>
         * * обычное limit \ offset с сортировкой по какому-либо полю и индексом
         *
         * @param last  последняя обработанная сущность предыдущем запросе
         * @param limit ограничение по кол-ву строк в результирующей выборке
         * @param offset оффсет
         * @return списов внешних сущностей
         */
        abstract List<E> fetchNextPart(E last, int limit, int offset);

    }
}
