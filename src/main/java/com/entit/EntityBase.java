package com.entit;

import org.bson.types.ObjectId;

/**
 * Created by andrius on 10/09/2017.
 */
public class EntityBase {

    private ObjectId _id;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }
}
