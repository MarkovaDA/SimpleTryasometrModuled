package ru.alidi.horeca.jobrunner.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileService {

    @Autowired
    private MongoTemplate mongoTemplate;
    public static final String METADATA_IMAGE_KEY = "is_image";
    public static final String METADATA_SITEMAP_KEY = "is_sitemap_file";
    private static Logger log = LoggerFactory.getLogger(FileService.class);
    
    
    @Autowired
    private GridFsTemplate gridFsTemplate;

    public GridFSFile saveSitemapFile(MultipartFile file) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_SITEMAP_KEY, true);
        final GridFSFile store = this.gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
        return store;
    }
    
    public GridFSFile saveSitemapStream(InputStream stream) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_SITEMAP_KEY, true);
        final GridFSFile store = this.gridFsTemplate.store(stream, "sitemap_" + DateTime.now().toString() + ".xml", "text/xml", metadata);
        return store;
    }

    public void deleteSitemapFile(String fileId) {
        gridFsTemplate.delete(getQueryById(fileId));
    }

    public GridFSDBFile getSitemapFile(String fileId) {
        GridFSDBFile gridFile = gridFsTemplate.findOne(getQueryById(fileId));
        if (gridFile != null) {
            if ((boolean) gridFile.getMetaData().get(METADATA_SITEMAP_KEY)) {
                return gridFile;
            } else {
                log.error("BLOB is not sitemap file, id {}", fileId);
            }
        } else {
            log.info("Cannot find sitemap file with id {}", fileId);
        }
        return null;
    }

    private Query getQueryById(String id) {
        return Query.query(Criteria.where("_id").is(new ObjectId(id)));
    }
    
    public void clearSitemapData(){       
       Query deleteQuery = new Query();
       DBObject dbObject = (DBObject)JSON.parse("{'_class':'java.util.HashMap', 'is_sitemap_file':true}");
       deleteQuery.addCriteria(Criteria.where("metadata").is(dbObject));
       gridFsTemplate.delete(deleteQuery);        
    }
    //удаление файлика из mongo по id
    public void deleteSitemapById(String id){
        Query deleteQuery = new Query();
        deleteQuery.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
        gridFsTemplate.delete(deleteQuery);
    }
}
