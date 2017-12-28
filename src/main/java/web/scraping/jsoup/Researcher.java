package web.scraping.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Researcher implements Comparable {

    private String key;
    private String name;
    private String view;

    public Researcher(String key, String name, String view) {
        this.key = key;
        this.name = name;
        this.view = view;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public static List<String> getProfileLinks() throws IOException {

        List<String> profileLinks = new ArrayList<>();

        Document researchers = Jsoup
                .connect(Utils.URL_FORM)
                .data("text2search", "%%%") // SQL injection to find all researchers
                .data("en", "1")    // To select search by name of researcher
                .maxBodySize(10 * 1024 * 1024)  // Body to 10MB indicated in Bytes
                .post();

        Elements elements = researchers.select("td.data a");
        int i = 0;

        for(Iterator<Element> iterator = elements.iterator(); iterator.hasNext(); i++) {

            Element researcher = iterator.next();

            if(i % 2 != 1) {

                String link = researcher.attr("href");

                if(link.contains("sis_showpub.php")) {

                    profileLinks.add(Utils.URL_BASE + link);
                }
            }
        }

        return profileLinks;
    }

    static String getKeyByResearcher(String researcherLink) throws IOException {

        String key = null;

        System.out.println("Searching key in URL: " + researcherLink + "...");

        Document document = Jsoup
                .connect(researcherLink)
                .maxBodySize(10 * 1024 * 1024)  // Body to 10MB indicated in Bytes
                .get();

        Elements keys = document.getElementsByAttributeValueStarting("href", "https://orcid.org");

        if(keys.size() > 0) {
            key = keys.first().text();
        } else {
            key = researcherLink.split("idpers=")[1].split("&")[0];
        }

        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Researcher that = (Researcher) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return view != null ? view.equals(that.view) : that.view == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (view != null ? view.hashCode() : 0);
        return result;
    }


    @Override
    public int compareTo(Object o) {

        if(!(o instanceof Researcher)) {
            return -1;
        }

        Researcher other = (Researcher) o;

        int compare = this.key.compareToIgnoreCase(other.key);

        if(compare == 0) {
            compare = this.name.compareToIgnoreCase(other.name);
        }

        if(compare == 0) {
            compare = this.view.compareTo(other.view);
        }

        return compare;
    }
}
