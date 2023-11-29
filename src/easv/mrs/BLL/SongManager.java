package easv.mrs.BLL;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.util.SongSearcher;
import easv.mrs.DAL.ISongDataAccess;
import easv.mrs.DAL.db.PlaylistSongDAO_DB;
import easv.mrs.DAL.db.SongDAO_DB;

import java.io.IOException;
import java.util.List;


public class SongManager {

    private final SongSearcher songSearcher = new SongSearcher();

    private final ISongDataAccess songDAO;
    private final PlaylistSongDAO_DB playlistSongDAO;

    public SongManager() throws IOException {
        songDAO = new SongDAO_DB();
        playlistSongDAO = new PlaylistSongDAO_DB();
    }
    //deleteSongFromPlaylist
    public List<Song> getAllSongs() throws Exception {
        return songDAO.getAllSongs();
    }

    public List<Song> getAllSongsPlaylist(Playlist playlist) throws Exception {

        return playlistSongDAO.getAllSongsPlaylist(playlist);

    }
    public void addSongToPlaylist(Song song, Playlist playlist) throws Exception {
        playlistSongDAO.addSongToPlaylist(song, playlist);
    }

    public void deleteSongFromPlaylist(Song song, Playlist playlist) throws Exception {
        playlistSongDAO.deleteSongFromPlaylist(song, playlist);
    }


    public List<Song> searchSongs(String query) throws Exception {
        List<Song> allSongs = getAllSongs();
        return songSearcher.search(allSongs, query);
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
