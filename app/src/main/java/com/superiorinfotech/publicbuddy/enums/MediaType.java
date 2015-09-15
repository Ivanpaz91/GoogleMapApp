package com.superiorinfotech.publicbuddy.enums;

/**
 * Created by alex on 17.01.15.
 */
public enum MediaType {
    PHOTO,
    VIDEO,
    AUDIO;

    public static MediaType getType(String type){
        if(type.equalsIgnoreCase("photo")){
            return PHOTO;
        }

        if(type.equalsIgnoreCase("video")){
            return VIDEO;
        }

        if(type.equalsIgnoreCase("audio")){
            return AUDIO;
        }

        return null;
    }
}
