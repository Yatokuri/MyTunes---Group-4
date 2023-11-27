package easv.mrs.BE;

public class Song {

    private int id;
    private String title;
    private int year;
    private String artist;
    private String songPath;
    public Song(int id, int year, String title, String artist, String songPath) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.artist = artist;
        this.songPath = songPath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getSongPath() { return songPath;}

    public void setSongPath() { this.songPath = songPath;}

    @Override
    public String toString()
    {
        return id + ": " + title + " ("+year+")";
    }
}
