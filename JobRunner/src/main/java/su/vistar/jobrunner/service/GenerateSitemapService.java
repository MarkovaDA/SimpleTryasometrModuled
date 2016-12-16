package ru.alidi.horeca.jobrunner.service;

import com.mongodb.gridfs.GridFSFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.alidi.horeca.common.dto.SitemapXml.XmlUrl;
import ru.alidi.horeca.common.dto.SitemapXml.XmlUrlSet;
import ru.alidi.horeca.common.dto.SitemapindexXml.XmlSitemap;
import ru.alidi.horeca.common.dto.SitemapindexXml.XmlSitemapindex;
import ru.alidi.horeca.persistence.dao.SitemapMongoDao;
import ru.alidi.horeca.persistence.dao.view.CategoryShortDao;
import ru.alidi.horeca.persistence.dao.view.ItemShortViewDao;
import ru.alidi.horeca.persistence.entity.view.CategoryShortViewEntity;
import ru.alidi.horeca.persistence.entity.view.ItemShortViewEntity;

@Qualifier("generateSitemapService")
@Service
public class GenerateSitemapService {
    
    private static final Logger log = LoggerFactory.getLogger(GenerateSitemapService.class);
    
    /* ID категории самого верхнего уровня */
    private static final Long TOP_CATEGORY_ID = 0l;
    
    /* Заключительная часть URL общей страницы акций */
    private static final String GENERAL_OFFERS_LINK = "offers";
    
    @Autowired
    private SitemapMongoDao sitemapMongoDao;
    
    @Autowired
    ItemShortViewDao itemShortViewDao;
    
    @Autowired
    CategoryShortDao categoryShortDao;
    
    private final String getSitemapFileUrl = "sitemap/";
    
    @Autowired
    private FileService fileService;
    
    @Value("${ru.alidi.horeca.host.url:http://localhost:8080/}")
    private String siteUrl;
    
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public ByteArrayOutputStream generateSitemap() throws JAXBException, IOException{
        log.info("Выполняем полную регенерацию сайтмепов");
        XmlSitemapindex xmlSitemapIndex = new XmlSitemapindex();
        List<XmlUrlSet> xmlUrlSets = new ArrayList();
        
        xmlUrlSets.add(getGeneralSitemap());
        xmlUrlSets.add(getItemsSitemap());
        xmlUrlSets.add(getCategorySitemap());
        
        List<String> mongoUids = new ArrayList<>();
        
        GridFSFile savedFile;
        // Сохранение файлов sitemap в mongo
        for (XmlUrlSet urlSet : xmlUrlSets){
            savedFile = fileService.saveSitemapStream(new ByteArrayInputStream(xmlToByteArray(urlSet)));   
            xmlSitemapIndex.addUrl(new XmlSitemap(siteUrl + getSitemapFileUrl + savedFile.getId().toString()));  
            mongoUids.add(savedFile.getId().toString());
        }
        //сохранение индексного файла
        ByteArrayOutputStream outputStream = xmlToByteArrayOutputStream(xmlSitemapIndex);
        savedFile = fileService.saveSitemapStream(new ByteArrayInputStream(outputStream.toByteArray()));
        String indexUid = savedFile.getId().toString();
        
        List<String> oldMongoIds = sitemapMongoDao.findAll();/*текущие айдишники сайтмепов,которые хранятся в базе*/
        //удаление старых айдишников из постгрес
        sitemapMongoDao.deleteAll();        
        //сохранение новых id-шников в постгрес из списка mongoUids
        sitemapMongoDao.saveSitemapMongoUids(mongoUids);
        //сохранение id-шника sitemap index файла в постгрес
        sitemapMongoDao.saveIndexMongoUid(indexUid);
        //удаление старых файлов из монго        
        oldMongoIds.forEach(item -> {
            //соответствующий файл из монго
            fileService.deleteSitemapById(item);
        });         
        return outputStream;
    }
    
    public XmlUrlSet getGeneralSitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();               
        createMainSitemap(xmlUrlSet);
        createGeneralOffersSitemap(xmlUrlSet);               
        return xmlUrlSet;
    }
    
    @Transactional
    public XmlUrlSet getItemsSitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();       
        createItemSitemap(xmlUrlSet);      
        return xmlUrlSet;
    }
    
    @Transactional
    public XmlUrlSet getCategorySitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();       
        createCategorySitemap(xmlUrlSet);      
        return xmlUrlSet;
    }
    
    public void createMainSitemap(XmlUrlSet xmlUrlSet) {
        create(xmlUrlSet, "", XmlUrl.Priority.HIGH);
    }
   
    @Transactional
    public void createItemSitemap(XmlUrlSet xmlUrlSet) {
        List<ItemShortViewEntity> items = itemShortViewDao.getAll();
        items.stream().forEach((item) -> {
            create(xmlUrlSet, "item/" + createItemLink(item), XmlUrl.Priority.MEDIUM);
        });
        
    }
    
    @Transactional
    public void createCategorySitemap(XmlUrlSet xmlUrlSet) {
        List<CategoryShortViewEntity> categories = categoryShortDao.findByParentIdOrderByOrder(TOP_CATEGORY_ID);
        categories.stream().forEach((category) -> {
            create(xmlUrlSet, "category/" + category.getUrl(), XmlUrl.Priority.MEDIUM);
        });
        
    }
    
    public void createGeneralOffersSitemap(XmlUrlSet xmlUrlSet) {
        create(xmlUrlSet, GENERAL_OFFERS_LINK, XmlUrl.Priority.MEDIUM);
    }
    
    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    private void create(XmlUrlSet xmlUrlSet, String link, XmlUrl.Priority priority) {
        xmlUrlSet.addUrl(new XmlUrl(siteUrl + link, priority.getValue()));
    }
    
    /**
     * Заключительная часть URL товара выглядит как <url> + "_" + <id>. 
     * Этот метод формирует текст заданного вида.
     */
    private String createItemLink(ItemShortViewEntity item) {
        return item.getUrl() + "_" + item.getId().toString();
    }
    
    private byte[] xmlToByteArray(Object jaxbElement) throws JAXBException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXBContext jaxbc = JAXBContext.newInstance(jaxbElement.getClass());
        Marshaller marshaller = jaxbc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(jaxbElement, outputStream);
        return outputStream.toByteArray();
    }
    
    private ByteArrayOutputStream xmlToByteArrayOutputStream(Object jaxbElement) throws JAXBException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXBContext jaxbc = JAXBContext.newInstance(jaxbElement.getClass());
        Marshaller marshaller = jaxbc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(jaxbElement, outputStream);
        return outputStream;
    }
}
