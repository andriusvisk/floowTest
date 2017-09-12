package com.entit;

import org.bson.types.ObjectId;

/**
 * Created by andrius on 10/09/2017.
 */
public class EntityBase {

    private ObjectId id;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
