package web.scheduled;

import com.google.gson.Gson;
import org.bson.Document;
import web.database.MongoConnector;
import web.scraping.jsoup.Chapter;
import web.scraping.jsoup.Researcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static web.database.MongoConnector.CHAPTERS_COLLECTION;
import static web.database.MongoConnector.CHAPTERS_NEW_COLLECTION;

public class Batcher {

    // MARK: Batcher settings

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day


    // MARK: Mongo

    private final MongoConnector mongoConnector = new MongoConnector();


    public void prepare() {

        final Runnable scheduled = this::start;

        scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

//        generateAllChapters();

        generateNewsChapters();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void generateAllChapters() {

        List<String> profileLinks = null;
        Set<Chapter> chapters = new HashSet<>();
        List<Document> documents = new ArrayList<>();

        try {
            profileLinks = Researcher.getProfileLinks();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

            chapters.forEach(chapter -> {
                Gson gson = new Gson();
                String json = gson.toJson(chapter);
                documents.add(Document.parse(json));
            });

            mongoConnector.cleanCollection(CHAPTERS_COLLECTION);

            mongoConnector.populateCollection(CHAPTERS_COLLECTION, documents);
        }
    }

    private void generateNewsChapters() {

        List<String> profileLinks = null;
        List<Document> documents = new ArrayList<>();
        Set<Chapter> chapters = new HashSet<>();
        Set<Chapter> oldChapters = mongoConnector.getChapters();

        try {
            profileLinks = Researcher.getProfileLinks();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

            Set<Chapter> newsChapters = chapters.stream().filter(x -> !oldChapters.contains(x)).collect(Collectors.toSet());

            newsChapters.forEach(chapter -> {
                Gson gson = new Gson();
                String json = gson.toJson(chapter);
                documents.add(Document.parse(json));
            });

            mongoConnector.cleanCollection(CHAPTERS_NEW_COLLECTION);

            mongoConnector.populateCollection(CHAPTERS_NEW_COLLECTION, documents);
        }
    }
}
