package com.vuducminh.nicefoodserver.model;

public class ShipperModel {
    private String key,uid,name,phone;
    private boolean active;

    public ShipperModel() {
    }

    public ShipperModel(String key, String uid, String name, String phone, boolean active) {
        this.key = key;
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.active = active;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
