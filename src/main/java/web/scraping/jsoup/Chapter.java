package web.scraping.jsoup;

import com.github.slugify.Slugify;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class Chapter {

    private String idChapter;
    private Book book;
    private String name;
    private SortedSet<Researcher> researchers;
    private String pages;
    private String viewURL;

    public static List<String> researchersKeys = new ArrayList<>();

    public Chapter(Book book, String name, SortedSet<Researcher> researchers, String pages, String viewURL) {
        this.book = book;
        this.name = name;
        this.researchers = researchers;
        this.pages = pages;
        this.viewURL = viewURL;
        this.idChapter = this.getIdChapter();
        this.viewURL = this.getViewURL();
    }

    public String getIdChapter() {

        Slugify slugify = new Slugify();
        StringBuilder researchersNames = new StringBuilder();

        this.researchers.forEach((researcher -> {
            researchersNames.append(researcher.getName());
        }));

        return slugify.slugify(this.book.getKey() + Utils.getFirstLetters(this.name) + Utils.getFirstLetters(researchersNames.toString()));
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedSet<Researcher> getResearchers() {
        return researchers;
    }

    public void setResearchers(SortedSet<Researcher> researchers) {
        this.researchers = researchers;
    }

    public String getViewURL() {
        return "http://si1718-npg-chapters.herokuapp.com/#!/chapters/" + this.getIdChapter() + "/view";
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public static Set<Chapter> getChaptersByResearcher(String researcherLink) throws IOException {

        Set<Chapter> chapters = new HashSet<>();

        System.out.println("Searching chapters in URL: " + researcherLink + "...");

        Document document = Jsoup
                .connect(researcherLink)
                .maxBodySize(10 * 1024 * 1024)  // Body to 10MB indicated in Bytes
                .get();


        // Improve execution time with this part of function from Researcher.java
        Elements keys = document.getElementsByAttributeValueStarting("href", "https://orcid.org");
        String key = null;

        if(keys.size() > 0) {
            key = keys.first().text();
        } else {
            key = researcherLink.split("idpers=")[1].split("&")[0];
        }
        // Finish to improve execution time

        Elements bookChapters = document.select("h5:matches(^Capítulos en Libros$)");

        if (bookChapters.size() > 0) {

            Element bookChapter = bookChapters.first();
            Element parent = bookChapter.parent();
            String html = parent.html();

            String[] data = html.split("<h5>");
            String bookChaptersContent = "";

            for(String aData : data) {

                if (aData.contains("Capítulos en Libros") && !aData.contains("Otra participación en Capítulos en Libros")) {
                    bookChaptersContent = aData.replace("\n", "");
                }
            }

            String[] bookChaptersElements = {};

            if (!bookChaptersContent.isEmpty()) {

                bookChaptersContent = bookChaptersContent.split("</h5>")[1];
                bookChaptersElements = bookChaptersContent.split("<br><br>");

                for (String bookChapterElement : bookChaptersElements) {

                    if (!bookChapterElement.trim().isEmpty()) {

                        String researchers;

                        if (bookChapterElement.contains("</u>")) {

                            researchers = bookChapterElement.split("</u>")[0];
                            researchers = researchers.replace(":", "");
                            researchers = researchers.replace("<u>", "");
                            researchers = researchers.trim();

                        } else {
                            researchers = "Undefined";
                        }

                        String name;

                        if (bookChapterElement.contains("<br>")) {

                            name = bookChapterElement.split("<br>")[1];
                            name = name.split("\\.")[0];
                            name = name.trim();

                        } else {
                            name = "Undefined";
                        }

                        String pages;

                        if (bookChapterElement.contains("Pag.")) {

                            pages = bookChapterElement.split("Pag.")[1];
                            pages = pages.split("\\.")[0];
                            pages = pages.trim();

                        } else {
                            pages = "Undefined";
                        }

                        String bookKey;

                        if (bookChapterElement.contains("ISBN")) {

                            bookKey = bookChapterElement.split("ISBN")[1];
                            bookKey = bookKey.trim();

                        } else {
                            bookKey = "Undefined";
                        }

                        SortedSet<Researcher> researchersSet = new TreeSet<>();

                        if(researchers.contains(",")) {

                            String[] researchersSplit = researchers.split(",");

                            int researchersSplitLength = researchersSplit.length;
                            int limit = (researchersSplitLength % 2 == 0) ? researchersSplitLength : researchersSplitLength - 1;

                            String researcherName;

                            for(int i = 0; i < limit; i += 2) {

                                researcherName = researchersSplit[i + 1].trim() + " " + researchersSplit[i].trim();

                                Researcher researcher = new Researcher(null, researcherName, null);

                                researchersSet.add(researcher);
                            }

                            if(researchersSplitLength % 2 == 1) {

                                researcherName = researchersSplit[researchersSplitLength - 1].trim();

                                Researcher researcher = new Researcher(null, researcherName, null);

                                researchersSet.add(researcher);
                            }
                        }

                        Book book = new Book(bookKey, null, null);

                        Chapter chapter = new Chapter(book, name, researchersSet, pages, null);
                        chapters.add(chapter);

                        researchersKeys.add(key);
                    }
                }

            } else {
                System.err.println("Error to recognize book chapters content.");
            }

        } else {
            System.err.println("This researcher doesn't have any chapter published in books.");
        }

        return chapters;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "book=" + book +
                ", name='" + name + '\'' +
                ", researchers=" + researchers +
                ", pages='" + pages + '\'' +
                ", viewURL='" + viewURL + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chapter chapter = (Chapter) o;

        if (book != null ? !book.equals(chapter.book) : chapter.book != null) return false;
        return name != null ? name.equals(chapter.name) : chapter.name == null;
    }

    @Override
    public int hashCode() {
        int result = book != null ? book.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
