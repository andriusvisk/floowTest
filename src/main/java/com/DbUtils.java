package com;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by andrius on 08/09/2017.
 */
public class DbUtils {

    Parameters parameters;

    public DbUtils(Parameters parameters) {
        this.parameters = parameters;
    }

    public void test() {
        MongoClient mongoClient = new MongoClient(parameters.getMongoHost(), parameters.getMongoPort());
        MongoDatabase mongoDatabase = mongoClient.getDatabase(parameters.getMongoDatabase());

        /*FindIterable<Document> mydatabaserecords = mongoDatabase.getCollection("collectionName1").find();
        MongoCursor<Document> iterator = mydatabaserecords.iterator();

        while (iterator.hasNext()) {
            Document doc = iterator.next();
            String name = doc.getString("name");
            int stop = 0;
        }*/

        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("collectionName1");
        String json = "{'name' : 'Andrius','lastName' : 'Viskontas'}";
        Document doc = Document.parse(json);
        mongoCollection.insertOne(doc);

    }
}
