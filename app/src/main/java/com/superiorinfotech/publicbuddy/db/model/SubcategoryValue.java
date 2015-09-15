package com.superiorinfotech.publicbuddy.db.model;

/**
 * Created by alex on 29.01.15.
 */
public class SubcategoryValue {
    private Long serverID;
    private String value;

    public SubcategoryValue(Long serverID, String value){
        this.serverID = serverID;
        this.value = value;
    }

    public Long getServerID() {
        return serverID;
    }

    public String getValue() {
        return value;
    }

    public String toString(){
        return value;
    }
}
