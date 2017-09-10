package com;

import com.entit.EntityBase;
import com.google.gson.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by andrius on 08/09/2017.
 */
public class DbUtils {

    Parameters parameters;

    public DbUtils(Parameters parameters) {
        this.parameters = parameters;
    }

    public <T> void insertOne(T entity) {
        MongoCollection<Document> mongoCollection = getMongoCollection(CollectionsEnum.getCollectionByClass(entity.getClass()));
        mongoCollection.insertOne(createDocument(entity));
    }

    public <T> List<T> find(Class<T> classs, String field, String value) {
        List<T> ret = new ArrayList<>();
        MongoCollection<Document> mongoCollection = getMongoCollection(CollectionsEnum.getCollectionByClass(classs));
        BasicDBObject query = (field != null) ? new BasicDBObject(field, value) : new BasicDBObject();
        FindIterable<Document> search = mongoCollection.find(query);
        for (Document current : search) {
            ret.add(createEntity(current, classs));
        }
        return ret;
    }

    public <T> List<T> findAll(Class<T> classs) {
        return find(classs, null, null);
    }

    public <T> T findOne(Class<T> classs) {
        MongoCollection<Document> mongoCollection = getMongoCollection(CollectionsEnum.getCollectionByClass(classs));
        BasicDBObject query = new BasicDBObject();
        Document search = mongoCollection.find(query).first();
        if (search != null) {
            return createEntity(search, classs);
        }else {
            return null;
        }
    }

    public <T> void updateById(T entity) {
        MongoCollection<Document> mongoCollection = getMongoCollection(CollectionsEnum.getCollectionByClass(entity.getClass()));
        ObjectId id = ((EntityBase) entity).get_id();
        ((EntityBase) entity).set_id(null);

        Document doc = createDocument(entity);

        mongoCollection.updateOne(new Document("_id", id), new Document("$set", doc));
    }

    public <T> void deleteById(T entity) {
        MongoCollection<Document> mongoCollection = getMongoCollection(CollectionsEnum.getCollectionByClass(entity.getClass()));
        String id = ((EntityBase) entity).get_id().toString();
        mongoCollection.deleteOne(new Document("_id", new ObjectId(id)));
    }

    private <T> Document createDocument(T entity) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(entity);
        return Document.parse(jsonStr);
    }

    private <T> T createEntity(Document document, Class<T> classs) {
        JsonDeserializer<ObjectId> des = new JsonDeserializer<ObjectId>() {
            @Override
            public ObjectId deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                return new ObjectId(je.getAsJsonObject().get("$oid").getAsString());
            }
        };
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, des).create();
        Object obj = gson.fromJson(document.toJson(), classs);
        return (T) obj;
    }

    private MongoCollection<Document> getMongoCollection(String collection) {
        MongoClient mongoClient = new MongoClient(parameters.getMongoHost(), parameters.getMongoPort());
        MongoDatabase mongoDatabase = mongoClient.getDatabase(parameters.getMongoDatabase());
        //TODO slaptazodis kaip array padaryti
        return mongoDatabase.getCollection(collection);
    }

    public Long getMongoDbLocalTimeInMs() {
        MongoClient mongoClient = new MongoClient(parameters.getMongoHost(), parameters.getMongoPort());
        MongoDatabase database = mongoClient.getDatabase("admin");
        Document serverStatus = database.runCommand(new Document("serverStatus", 1));
        Date localTime = (Date) serverStatus.get("localTime");
        return localTime.getTime();
    }

}
