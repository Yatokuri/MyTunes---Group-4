package easv.mrs.BE;

import easv.mrs.DAL.db.SongDAO_DB;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Playlist {



    private String playlistName;
    private int id;
    private int songCount;
    private double songTotalTime;

    public Playlist(int id, String playlistName, int songCount, double songTotalTime){
        this.id = id;
        this.playlistName = playlistName;
        this.songCount = songCount;
        this.songTotalTime = songTotalTime;


        //System.out.println("Ny playlister");
    }





    public double getSongTotalTime() { return songTotalTime; }

    public void setSongTotalTime(double songTotalTime) {this.songTotalTime = songTotalTime;}
    public int getId() { return id;}
    public String getPlaylistName() { return playlistName; }
    public void setPlaylistName(String playlistName) { this.playlistName = playlistName; }

    public int getSongCount() {

                return songCount;

    }

        public void setSongCount(int songCount) {this.songCount = songCount;}
}
