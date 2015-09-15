package com.superiorinfotech.publicbuddy.api;

import com.google.gson.JsonObject;

/**
 * Created by alex on 21.01.15.
 */
public class ApiUser {
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  userName;
    private String  password;
    private String  phone;
    private Long    opStatus;
    private String  msgReturned;
    private Boolean tempPasswordYN;
    private String  entityPin;

    public ApiUser(JsonObject jUser){
        firstName = jUser.get("firstName").getAsString();
        lastName  = jUser.get("lastName").getAsString();
        email     = jUser.get("email").getAsString();
        userName  = jUser.get("userName").getAsString();
        password  = jUser.get("password").getAsString();
        phone     = jUser.get("phone").getAsString();
        opStatus  = jUser.get("opStatus").getAsLong();
        msgReturned = jUser.get("msgReturned").getAsString();
        tempPasswordYN = jUser.get("tempPasswordYN").getAsBoolean();
        entityPin      = jUser.get("entityPin").getAsString();

    }

//-----PUBLIC METHODS------------------------------------------------------------------------------
    public Boolean isTemporaryPassword(){
        return tempPasswordYN;
    }
}
