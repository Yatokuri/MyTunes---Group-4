/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Song;
import easv.mrs.DAL.ISongDataAccess;
import easv.mrs.DAL.db.SongDAO_DB;

import java.io.IOException;
import java.util.List;

public class SongManager {

    private final ISongDataAccess songDAO;

    public SongManager() throws IOException {
        songDAO = new SongDAO_DB();
    }

    public Song createNewSong(Song newSong) throws Exception {
        return songDAO.createSong(newSong);
    }

    public List<Song> getAllSongs() throws Exception {
        return songDAO.getAllSongs();
    }

    public void updateSong(Song selectedSong) throws Exception {
        songDAO.updateSong(selectedSong);
    }

    public void deleteSong(Song selectedSong) throws Exception {
        songDAO.deleteSong(selectedSong);
    }
}
