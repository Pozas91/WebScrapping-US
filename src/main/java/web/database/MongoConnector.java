package web.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import web.scraping.jsoup.Chapter;
import web.scraping.jsoup.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoConnector {

    // MARK: Mongo data
    private final MongoClientURI uri = new MongoClientURI(Utils.URL_DATABASE);
    private MongoClient client;
    private MongoDatabase database;

    // MARK: Databases
    private static final Integer MAX_BATCH_TO_SAVE = 500;
    private static final Integer MAX_BATCH_TO_GET = 500;

    // MARK: Collections
    public static final String CHAPTERS_COLLECTION = "chapters";
    public static final String CHAPTERS_NEW_COLLECTION = "chapters_new";


    // MARK: Getters

    public Set<Chapter> getChapters() {

        Set<Chapter> chapters = new HashSet<>();
        GsonBuilder builder = new GsonBuilder().serializeNulls();
        Gson gson = builder.create();
        List<Document> documents = new ArrayList<>();
        BasicDBObject sort = new BasicDBObject("_id", -1);
        int limit;

        openDatabase();

        MongoCollection<Document> collection = this.getCollection(CHAPTERS_COLLECTION);

        final int total = Math.toIntExact(collection.count());

        for(int i = 0; documents.size() < total; i += MAX_BATCH_TO_GET) {

            limit = ((i + MAX_BATCH_TO_GET) < total) ? MAX_BATCH_TO_GET : (total - i);

            List<Document> batch = collection.find().sort(sort).skip(i).limit(limit).into(new ArrayList<>());

            documents.addAll(batch);
        }

        closeDatabase();

        documents.forEach(document -> {

            try {

                Chapter chapter = gson.fromJson(document.toJson(), Chapter.class);
                chapters.add(chapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return chapters;
    }


    // MARK: Aux functions

    public void populateCollection(String collection, List<Document> data) {

        openDatabase();

        List<List<Document>> batches = Utils.batchList(data, MAX_BATCH_TO_SAVE);

        batches.forEach(batch -> {
            this.getCollection(collection).insertMany(batch);
        });

        closeDatabase();
    }

    public void cleanCollection(String collection) {

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", collection);

        this.openDatabase();

        this.getCollection(collection).drop();

        this.closeDatabase();

        System.out.printf("%n Data cleaned. %n");
    }


    // MARK: Private functions

    private MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }

    private void openDatabase() {
        this.client = new MongoClient(uri);
        this.database = this.client.getDatabase(uri.getDatabase());
    }

    private void closeDatabase() {
        this.client.close();
    }
}
