/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Song;
import easv.mrs.DAL.ISongDataAccess;
import easv.mrs.DAL.db.SongDAO_DB;

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
}
