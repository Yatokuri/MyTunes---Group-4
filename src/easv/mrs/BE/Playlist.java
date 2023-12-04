package easv.mrs.BE;

public class Playlist {

    private String playlistName, songTotalTimeHHMMSS;
    private int id;
    private int songCount;
    private double songTotalTime;

    public Playlist(int id, String playlistName, int songCount, double songTotalTime){
        this.id = id;
        this.playlistName = playlistName;
        this.songCount = songCount;
        this.songTotalTime = songTotalTime;
    }

    public String getSongLengthHHMMSS() {
        long hours = (long) (songTotalTime / 3600);
        long minutes = (long) ((songTotalTime % 3600) / 60);
        long remainingSeconds = (long) (songTotalTime % 60);
        return songTotalTimeHHMMSS = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    public double getSongTotalTime() { return songTotalTime; }
    public void setSongTotalTime(double songTotalTime) {this.songTotalTime = songTotalTime;}
    public int getId() { return id;}
    public String getPlaylistName() { return playlistName; }
    public void setPlaylistName(String playlistName) { this.playlistName = playlistName; }
    public int getSongCount() {return songCount;}
    public void setSongCount(int songCount) {this.songCount = songCount;}}