/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.GUI.Model;

import easv.MyTunes.BE.Song;
import easv.MyTunes.BLL.SongManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class SongModel {

    private static ObservableList<Song> songsToBeViewed = null;
    private final SongManager songManager;

    public SongModel() throws Exception {
        songManager = new SongManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(songManager.getAllSongs());
    }
    public Song createNewSong(Song newSong) throws Exception { // Sends a request to the database to add a new song
        Song s = songManager.createNewSong(newSong);
        songsToBeViewed.add(s); // update list
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());
        return s;
    }
    public static ObservableList<Song> getObservableSongs() { return songsToBeViewed; } // Returns the songs from the database
    public void updateSong(Song updatedSong) throws Exception { // Sends a request to the database to update a song
        // update song in DAL layer (through the layers)
        songManager.updateSong(updatedSong);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());
    }
    public ObservableList<Song> updateSongList() throws Exception { // Updates the song list from the database to be accurate again
        songsToBeViewed.addAll(songManager.getAllSongs());
        return songsToBeViewed;
    }
    public void deleteSong(Song selectedSong) throws Exception { // Sends a request to the database to delete a song
        // delete song in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);
        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }
    public ObservableList<Song> filterList(List<Song> song, String searchText){ // Gets the search filter from the Song Manager to give to the Controller
        return songManager.filterList(song, searchText);

    }
    public Song getSongById(int songId){ // Returns a song by its id
        return songManager.getSongById(songId);
    }
}
