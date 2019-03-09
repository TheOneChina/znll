package com.tnsoft.web.model;

import java.io.Serializable;

public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer nonce;
    private String message;
    private int status;
    private Device device;

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setDevice(AuthResponse.Device device) {
        this.device = device;
    }

    public AuthResponse.Device getDevice() {
        return device;
    }


    public AuthResponse() {
        super();
    }

    public static class Device implements Serializable {

        private static final long serialVersionUID = 1L;

        private int productbatch_id;
        private String last_active;
        private int ptype = 12335;
        private int activate_status = 1;
        private String serial = "334a8481";
        private int id;
        private String bSSID;
        private String last_pull;
        private String location;
        private String metadata;
        private int status = 2;
        private String updated;
        private String description = "device-description-79eba060";
        private String activated_at;
        private int visibly = 1;
        private int is_private = 1;
        private int product_id = 1;
        private String name = "device-name-79eba060";
        private String created;
        private int is_frozen = 0;
        private int key_id = 387;

        public void setProductbatch_id(int productbatch_id) {
            this.productbatch_id = productbatch_id;
        }

        public int getProductbatch_id() {
            return productbatch_id;
        }

        public void setLast_active(String last_active) {
            this.last_active = last_active;
        }

        public String getLast_active() {
            return last_active;
        }

        public void setPtype(int ptype) {
            this.ptype = ptype;
        }

        public int getPtype() {
            return ptype;
        }

        public void setActivate_status(int activate_status) {
            this.activate_status = activate_status;
        }

        public int getActivate_status() {
            return activate_status;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getSerial() {
            return serial;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setBSSID(String bSSID) {
            this.bSSID = bSSID;
        }

        public String getBSSID() {
            return bSSID;
        }

        public void setLast_pull(String last_pull) {
            this.last_pull = last_pull;
        }

        public String getLast_pull() {
            return last_pull;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLocation() {
            return location;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public String getUpdated() {
            return updated;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setActivated_at(String activated_at) {
            this.activated_at = activated_at;
        }

        public String getActivated_at() {
            return activated_at;
        }

        public void setVisibly(int visibly) {
            this.visibly = visibly;
        }

        public int getVisibly() {
            return visibly;
        }

        public void setIs_private(int is_private) {
            this.is_private = is_private;
        }

        public int getIs_private() {
            return is_private;
        }

        public void setProduct_id(int product_id) {
            this.product_id = product_id;
        }

        public int getProduct_id() {
            return product_id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getCreated() {
            return created;
        }

        public void setIs_frozen(int is_frozen) {
            this.is_frozen = is_frozen;
        }

        public int getIs_frozen() {
            return is_frozen;
        }

        public void setKey_id(int key_id) {
            this.key_id = key_id;
        }

        public int getKey_id() {
            return key_id;
        }


    }
}
