package easv.mrs.GUI.Model;

import easv.mrs.BE.Song;
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



    public ObservableList<Song> getObservableMovies() {
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

        // loading entire file again... not optimal
        //moviesToBeViewed.clear();
        //moviesToBeViewed.addAll(movieManager.getAllMovies());
    }


    public void updateSong(Song updatedSong) throws Exception {
        // update movie in DAL layer (through the layers)
        songManager.updateSong(updatedSong);

        // update observable list (and UI)
        Song m = songsToBeViewed.get(songsToBeViewed.indexOf(updatedSong));
        m.setTitle(updatedSong.getTitle());
        m.setYear(updatedSong.getYear());
    }


    public void deleteMovie(Song selectedSong) throws Exception {
        // delete movie in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);

        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }
}
