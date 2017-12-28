package web.scraping.jsoup;

public class Book {

    private String key;
    private String title;
    private String view;

    public Book(String key, String title, String view) {
        this.key = key;
        this.title = title;
        this.view = view;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (key != null ? !key.equals(book.key) : book.key != null) return false;
        if (title != null ? !title.equals(book.title) : book.title != null) return false;
        return view != null ? view.equals(book.view) : book.view == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (view != null ? view.hashCode() : 0);
        return result;
    }
}
