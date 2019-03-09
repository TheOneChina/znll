package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Table(name = "binary_file")
public class BinaryFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int domainId;
    private int type;
    private int mimeType;
    private transient Blob content;
    private int status;
    private long csn;

    public BinaryFile() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "binary_file_id_seq")
    @SequenceGenerator(name = "binary_file_id_seq", sequenceName = "binary_file_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    @Column(name = "mime_type")
    public int getMimeType() {
        return mimeType;
    }

    @Lob
    @Column(name = "content")
    public Blob getContent() {
        return content;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    @Column(name = "csn")
    public long getCsn() {
        return csn;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCsn(long csn) {
        this.csn = csn;
    }

}
