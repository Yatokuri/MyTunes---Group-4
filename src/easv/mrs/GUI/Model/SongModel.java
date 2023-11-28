package easv.mrs.GUI.Model;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.PlaylistManager;
import easv.mrs.BLL.SongManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class SongModel {

    private ObservableList<Song> songsToBeViewed;

    private SongManager songManager;

    public SongModel() throws Exception {
        songManager = new SongManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(songManager.getAllSongs());

    }



    public ObservableList<Song> getObservableSongs() {
        return songsToBeViewed;
    }


    public void searchSong(String query) throws Exception {
        List<Song> searchResults = songManager.searchSongs(query);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(searchResults);
    }


    public void createNewSong(Song newSong) throws Exception {
        Song s = songManager.createNewSong(newSong);
        songsToBeViewed.add(s); // update list
    }


    public void updateSong(Song updatedSong) throws Exception {
        // update song in DAL layer (through the layers)
        songManager.updateSong(updatedSong);

        // update observable list (and UI)
        Song s = songsToBeViewed.get(songsToBeViewed.indexOf(updatedSong));
        s.setTitle(updatedSong.getTitle());
        s.setYear(updatedSong.getYear());
    }


    public void deleteSong(Song selectedSong) throws Exception {
        // delete song in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);

        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }


}
