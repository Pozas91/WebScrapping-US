package web.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import web.scraping.jsoup.Utils;

import java.util.List;

public class MongoConnection {

    public static final String CHAPTERS_COLLECTION = "chapters";
    public static final String CHAPTERS_AUX_COLLECTION = "chapters_aux";

    private final MongoClientURI uri = new MongoClientURI(Utils.URL_DATABASE);
    private MongoClient client;
    private MongoDatabase database;

    public MongoConnection() {
    }

    public void populateCollection(String collection, List<Document> data) {

        this.openDatabase();

        this.getCollection(collection).insertMany(data);

        this.closeDatabase();
    }

    public void cleanCollection(String collection) {

        this.openDatabase();

        this.getCollection(collection).drop();

        this.closeDatabase();
    }

    private void openDatabase() {
        this.client = new MongoClient(uri);
        this.database = this.client.getDatabase(uri.getDatabase());
    }

    private void closeDatabase() {
        this.client.close();
    }

    private MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }
}
