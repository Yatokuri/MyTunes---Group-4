/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BE;

public class Song {

    private String title, artist, songPath, songCategory;
    private int year;
    private double songLength;
    private int id;
       public Song(int id, int year, String title, String artist, String songPath, Double songLength, String category) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.artist = artist;
        this.songLength = songLength;
        this.songPath = songPath;
        this.songCategory = category;
    }
    public double getSongLength() {return songLength;}

    public void setSongLength(double songLength) {this.songLength = songLength;}

    public String getSongLengthHHMMSS() { // This way you convert songTotalTime to HH:MM:SS format
        long hours = (long) (songLength / 3600);
        long minutes = (long) ((songLength % 3600) / 60);
        long remainingSeconds = (long) (songLength % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    public int getId() {return id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public int getYear() {return year;}
    public void setYear(int year) {this.year = year;}
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getSongPath() { return songPath;}
    public void setSongPath(String songPath) { this.songPath = songPath;}
    @Override
    public String toString()
    {
        return id + ": " + title + " ("+year+")";
    }

    public String getSongCategory() { return songCategory; }
    public void setSongCategory(String songCategory){ this.songCategory = songCategory;}
}