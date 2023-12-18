/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BLL;

import easv.MyTunes.BE.Playlist;
import easv.MyTunes.BE.Song;
import easv.MyTunes.DAL.db.PlaylistSongDAO_DB;

import java.util.List;

public class PlaylistSongManager {

    private final PlaylistSongDAO_DB playlistSongDAO;

    public PlaylistSongManager() throws Exception {
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
