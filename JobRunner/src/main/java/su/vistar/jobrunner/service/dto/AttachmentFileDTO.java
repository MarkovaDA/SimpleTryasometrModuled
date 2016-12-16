package ru.alidi.horeca.jobrunner.service.dto;

import com.mongodb.gridfs.GridFSDBFile;
import java.io.InputStream;

/**
 *
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
public class AttachmentFileDTO {
    
    private String filename;
    private String contentType;
    private InputStream inputStream;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public AttachmentFileDTO(String filename, String contentType, InputStream stream) {
        this.filename = filename;
        this.contentType = contentType;
        this.inputStream = stream;
    }
    
    public AttachmentFileDTO(GridFSDBFile file) {
        this.filename = file.getFilename();
        this.contentType = file.getContentType();
        this.inputStream = file.getInputStream();
    }

    public AttachmentFileDTO() {}
    
}
