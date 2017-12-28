package web.scheduled;

import com.google.gson.Gson;
import org.bson.Document;
import web.database.MongoConnection;
import web.main.TestJSoup;
import web.scraping.jsoup.Chapter;
import web.scraping.jsoup.Researcher;
import web.scraping.jsoup.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Batcher {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        savingChaptersFromWebScrapping();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void savingChaptersFromWebScrapping() {

        List<String> profileLinks = null;

        try {
            profileLinks = Researcher.getProfileLinks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<Chapter> chapters = new HashSet<>();

        assert profileLinks != null;

        for(String profileLink : profileLinks) {
            try {
                chapters.addAll(Chapter.getChaptersByResearcher(profileLink));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(chapters.isEmpty()) {

            System.err.println("Don't have any data to record.");

        } else {

            List<Document> data = new ArrayList<>();

            chapters.forEach(chapter -> {
                Gson gson = new Gson();
                String json = gson.toJson(chapter);
                data.add(Document.parse(json));
            });

            String collection = MongoConnection.CHAPTERS_AUX_COLLECTION;

            MongoConnection mongoConnection = new MongoConnection();
            mongoConnection.cleanCollection(collection);

            List<List<Document>> batches = Utils.batchList(data, 150);

            for(List<Document> batch : batches) {
                mongoConnection.populateCollection(collection, batch);
            }
        }
    }
}
