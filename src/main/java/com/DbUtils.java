package com;

import com.entit.EntityBase;
import com.google.gson.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;


/**
 * Created by andrius on 08/09/2017.
 */
public class DbUtils {

    private MongoClient mongoClient;
    private CodecRegistry defaultCodecRegistry;
    private CodecProvider pojoCodecProvider;
    private CodecRegistry pojoCodecRegistry;
    private MongoDatabase mongoDatabase;
    private MongoDatabase databaseAdmin;

    public DbUtils(Parameters parameters) {
        init(parameters.getMongoHost(), parameters.getMongoPort(),
                parameters.getMongoDatabase(), parameters.getMongoUsername(), parameters.getMongoPassowrd());
    }

    public DbUtils(String mongoHost, int mongoPort, String mongoDb, String username, String password) {
        init(mongoHost, mongoPort, mongoDb, username, password);
    }

    public void closeConnections(){
        mongoClient.close();
    }

    private void init(String mongoHost, int mongoPort, String mongoDb, String username, String password) {

        /*List<ServerAddress> seeds = new ArrayList<>();
        seeds.add( new ServerAddress( mongoHost, mongoPort));
        List<MongoCredential> credentials = new ArrayList<>();
        credentials.add(
                MongoCredential.createMongoCRCredential(
                        username,
                        mongoDb,
                        password.toCharArray()
                )
        );*/

        mongoClient = new MongoClient(mongoHost, mongoPort); //new MongoClient( seeds, credentials );
        defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
        pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        pojoCodecRegistry = fromRegistries(defaultCodecRegistry, fromProviders(pojoCodecProvider));
        mongoDatabase = mongoClient.getDatabase(mongoDb).withCodecRegistry(pojoCodecRegistry);
        databaseAdmin = mongoClient.getDatabase("admin").withCodecRegistry(pojoCodecRegistry);
    }

    public <T> void insertOne(T entity) {
        MongoCollection<T> mongoCollection = getMongoCollection((Class<T>) entity.getClass());
        mongoCollection.insertOne((T) createDocument(entity));
    }

    public <T> List<T> find(Class<T> classs, String field, Object value) {
        List<T> ret = new ArrayList<>();
        MongoCollection<T> mongoCollection = getMongoCollection(classs);
        BasicDBObject query = (field != null) ? new BasicDBObject(field, value) : new BasicDBObject();
        return mongoCollection.find(query).into(new ArrayList<T>());
    }

    public <T> List<T> findAll(Class<T> classs) {
        return find(classs, null, null);
    }

    public <T> T findOne(Class<T> classs) {
        MongoCollection<T> mongoCollection = getMongoCollection(classs);
        BasicDBObject query = new BasicDBObject();
        List<T> list = mongoCollection.find(query).into(new ArrayList<T>());
        if (list != null) {
            if (list.size() > 0) {
                return list.get(0);
            }
        }
        return null;
    }

    public <T> void updateById(T entity) {
        MongoCollection<T> mongoCollection = getMongoCollection((Class<T>) entity.getClass());
        ObjectId id = ((EntityBase) entity).getId();
        ((EntityBase) entity).setId(null);

        Document doc = createDocument(entity);

        mongoCollection.updateOne(new Document("_id", id), new Document("$set", doc));
    }

    public <T> void deleteById(T entity) {
        MongoCollection<T> mongoCollection = getMongoCollection((Class<T>) entity.getClass());
        String id = ((EntityBase) entity).getId().toString();
        mongoCollection.deleteOne(new Document("_id", new ObjectId(id)));
    }

    private <T> Document createDocument(T entity) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(entity);
        return Document.parse(jsonStr);
    }

    private <T> MongoCollection<T> getMongoCollection(Class<T> classs) {
        String collection = CollectionsEnum.getCollectionByClass(classs);
        return mongoDatabase.getCollection(collection, classs);
    }

    public Long getMongoDbLocalTimeInMs() {
        Document serverStatus = databaseAdmin.runCommand(new Document("serverStatus", 1));
        Date localTime = (Date) serverStatus.get("localTime");
        return localTime.getTime();
    }

}
