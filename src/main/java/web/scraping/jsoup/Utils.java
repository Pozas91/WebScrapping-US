package web.scraping.jsoup;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final String URL_BASE = "http://investigacion.us.es";
    public static final String URL_FORM = URL_BASE + "/sisius/sisius.php";
    public static final String URL_DATABASE = "mongodb://pozas91:pozas91@ds149865.mlab.com:49865/si1718-npg-chapters";

    public static <T> List<List<T>> batchList(List<T> inputList, final int maxSize) {

        final int size = inputList.size();
        List<List<T>> subLists = new ArrayList<>();

        for(int i = 0; i < size; i += maxSize) {
            subLists.add(new ArrayList<>(inputList.subList(i, Math.min(size, i + maxSize))));
        }

        return subLists;
    }

    public static String getFirstLetters(String text) {

        StringBuilder firstLetters = new StringBuilder();

        text = text.replaceAll("[.,-]", ""); // Replace dots, etc (optional)

        for(String s : text.split(" ")) {

            try {
                firstLetters.append(s.charAt(0));
            } catch (Exception e) {
                System.err.println("Error: " + e.getLocalizedMessage() + ", in chartAt(0) about '" + s + "' string.");
            }
        }

        return firstLetters.toString().toLowerCase();
    }
}
