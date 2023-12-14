/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Song;
import easv.mrs.DAL.ISongDataAccess;
import easv.mrs.DAL.db.SongDAO_DB;
import easv.mrs.GUI.Model.SongModel;
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

    private boolean searchFindsSongs(Song song, String searchText) {
        return (song.getTitle().toLowerCase().contains(searchText.toLowerCase())) || (song.getArtist().toLowerCase().contains(searchText.toLowerCase()));
    }

    public ObservableList<Song> filterList(List<Song> song, String searchText) {
        List<Song> filterList = new ArrayList<>();
        for (Song s : song) {
            if (searchFindsSongs(s, searchText)) {
                filterList.add(s);
            }
        }
        return FXCollections.observableList(filterList);
    }

}
