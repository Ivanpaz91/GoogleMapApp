package com.superiorinfotech.publicbuddy.tasks;

/**
 * Created by alex on 20.01.15.
 */

import android.content.Context;
import android.os.AsyncTask;

import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Entity;
import com.superiorinfotech.publicbuddy.db.model.Organization;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This task is used to asynchronously parse JSON returned by WebService for Login request.
 * Response JSON contains information about Organization/Entity/Category/Subcategory which
 * current user has access to. This JSON must be parsed and:
 * - If DB has no such Organization - insert it
 * - If DB has no such Entity - insert it
 * - If DB has no such Category - insert it
 * - If DB has no Subcategory from the list - insert them
 * - Clear CredentialsMapper Table and create new entries for the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final JsonObject json;
    private Context context;
    User user;

    public UserLoginTask(Context context, JsonObject json, User user) {
        this.context = context;
        this.json = json;
        this.user = user;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //First we need to remove all old credentials for user
        user.clearCredentials();

        String organizationName = json.get("organization").getAsJsonObject().get("organizationName").getAsString();
        Long   organizationID   = json.get("organization").getAsJsonObject().get("organizationID").getAsLong();
        Organization org = Organization.newInstance(context, organizationID);
        if(org==null){
            org = Organization.createOrganization(context, String.valueOf(organizationID), organizationName);
        }
        org.givePermission(user);

        JsonArray jEntitiesList = json.get("entities").getAsJsonArray();
        for(JsonElement jEntityObject : jEntitiesList){
            JsonObject jEntity = jEntityObject.getAsJsonObject();
            Long id = jEntity.get("entityID").getAsLong();
            String name = jEntity.get("entityName").getAsString();
            Long orgID = jEntity.get("orgID").getAsLong();
            Entity entity = Entity.newInstance(context, id);
            if(entity==null){
                entity = Entity.createEntity(context, String.valueOf(id), name, String.valueOf(orgID));
            }
            entity.givePermission(user);
        }

        JsonArray jCategoriesList = json.get("categories").getAsJsonArray();
        for(JsonElement jCategoryObject : jCategoriesList){
            JsonObject jCategory = jCategoryObject.getAsJsonObject();
            Long id = jCategory.get("categoryID").getAsLong();
            String name = jCategory.get("categoryName").getAsString();
            Long entityID = jCategory.get("entityID").getAsLong();
            Category category = Category.newInstance(context, id);
            if(category==null){
                category = Category.createCategory(context, String.valueOf(id), name, String.valueOf(entityID));
            }
            category.givePermission(user);
        }

        JsonArray jSubCategoriesList = json.get("subCategories").getAsJsonArray();
        for(JsonElement jSubCategoryObject : jSubCategoriesList){
            JsonObject jSubCategory = jSubCategoryObject.getAsJsonObject();
            Long id = jSubCategory.get("subCategoryID").getAsLong();
            String name = jSubCategory.get("subCategoryName").getAsString();
            Long entityID = jSubCategory.get("categoryID").getAsLong();
            String value = jSubCategory.get("subCategoryNameValue").getAsString();
            SubCategory subCategory = SubCategory.newInstance(context, name);
            if(subCategory==null){
                subCategory = SubCategory.createSubCategory(context, name, String.valueOf(entityID));
            }
            subCategory.addValue(String.valueOf(id), value);
            subCategory.givePermission(user);
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {

    }

    @Override
    protected void onCancelled() {
//        mAuthTask = null;
    }
}