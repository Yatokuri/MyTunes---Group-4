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
    public Song createNewSong(Song newSong) throws Exception {
        Song s = songManager.createNewSong(newSong);
        songsToBeViewed.add(s); // update list
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());
        return s;
    }
    public static ObservableList<Song> getObservableSongs() { return songsToBeViewed; }
    public void updateSong(Song updatedSong) throws Exception {
        // update song in DAL layer (through the layers)
        songManager.updateSong(updatedSong);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());
    }
    public ObservableList<Song> updateSongList() throws Exception {
        songsToBeViewed.addAll(songManager.getAllSongs());
        return songsToBeViewed;
    }
    public void deleteSong(Song selectedSong) throws Exception {
        // delete song in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);
        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }
    public ObservableList<Song> filterList(List<Song> song, String searchText){
        return songManager.filterList(song, searchText);

    }
    public Song getSongById(int songId){
        return songManager.getSongById(songId);
    }
}
