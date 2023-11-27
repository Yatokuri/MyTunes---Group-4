package easv.mrs.BLL;

import easv.mrs.BE.Song;
import easv.mrs.BLL.util.SongSearcher;
import easv.mrs.DAL.ISongDataAccess;
import easv.mrs.DAL.db.SongDAO_DB;

import java.io.IOException;
import java.util.List;

public class SongManager {

    private SongSearcher songSearcher = new SongSearcher();

    private ISongDataAccess songDAO;

    public SongManager() throws IOException {
        songDAO = new SongDAO_DB();
    }

    public List<Song> getAllSongs() throws Exception {
        return songDAO.getAllSongs();
    }

    public List<Song> searchSongs(String query) throws Exception {
        List<Song> allSongs = getAllSongs();
        List<Song> searchResult = songSearcher.search(allSongs, query);
        return searchResult;
    }

    public Song createNewSong(Song newSong) throws Exception {
        return songDAO.createSong(newSong);
    }

    public void updateSong(Song selectedSong) throws Exception {
        songDAO.updateSong(selectedSong);
    }


    public void deleteSong(Song selectedSong) throws Exception {
        songDAO.deleteSong(selectedSong);
    }
}
