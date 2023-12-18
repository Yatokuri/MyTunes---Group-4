/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BLL;

import easv.MyTunes.BE.Song;
import easv.MyTunes.DAL.ISongDataAccess;
import easv.MyTunes.DAL.db.SongDAO_DB;
import easv.MyTunes.GUI.Model.SongModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SongManager {

    private final ISongDataAccess songDAO;
    private final SongDAO_DB songDao_DB;

    public SongManager() throws Exception {
        songDAO = new SongDAO_DB();
        songDao_DB = new SongDAO_DB();
    }

    public Song createNewSong(Song newSong) throws Exception {
        return songDAO.createSong(newSong);
    }

    public List<Song> getAllSongs() throws Exception {
        return songDao_DB.getAllSongs();
    }

    public void updateSong(Song selectedSong) throws Exception {
        songDAO.updateSong(selectedSong);
    }

    public void deleteSong(Song selectedSong) throws Exception {
        songDAO.deleteSong(selectedSong);
    }


    public Song getSongById(int songId) {
        for (Song s : SongModel.getObservableSongs()) {
            if (s.getId() == songId) {
                return s;
            }
        }
        return null;
    }

    private boolean searchFindsSongs(Song song, String searchText) { // Creates the search parameter for the title and artist column to use for the search filter
        return (song.getTitle().toLowerCase().contains(searchText.toLowerCase())) || (song.getArtist().toLowerCase().contains(searchText.toLowerCase()));
    }

    public ObservableList<Song> filterList(List<Song> song, String searchText) { // Creates an observable list for the search function in the GUI that
        List<Song> filterList = new ArrayList<>();                               // mirrors the song list but changes based on search input from the above method
        for (Song s : song) {
            if (searchFindsSongs(s, searchText)) {
                filterList.add(s);
            }
        }
        return FXCollections.observableList(filterList);
    }

}
