/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.DAL.db.PlaylistSongDAO_DB;

import java.io.IOException;
import java.util.List;

public class SongPlaylistManager {

    private final PlaylistSongDAO_DB playlistSongDAO;

    public SongPlaylistManager() throws IOException {
        playlistSongDAO = new PlaylistSongDAO_DB();
    }

    public void addSongToPlaylist(Song song, Playlist playlist) throws Exception {
        playlistSongDAO.addSongToPlaylist(song, playlist);
    }

    public List<Song> getAllSongsPlaylist(Playlist playlist) throws Exception {
        return playlistSongDAO.getAllSongsPlaylist(playlist);
    }

    public void updateSongInPlaylist(Song song, Song oldsong, Playlist playlist) throws Exception {
        playlistSongDAO.updateSongInPlaylist(song, oldsong, playlist);
    }

    public void deleteSongFromPlaylist(Song song, Playlist playlist) throws Exception {
        playlistSongDAO.deleteSongFromPlaylist(song, playlist);
    }

    public void deleteAllSongsFromPlaylist(Playlist playlist) throws Exception {
        playlistSongDAO.deleteAllSongsFromPlaylist(playlist);
    }

}
