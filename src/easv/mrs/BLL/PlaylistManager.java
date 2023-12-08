/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Playlist;
import easv.mrs.DAL.IPlaylistDataAccess;
import easv.mrs.DAL.db.PlaylistDAO_DB;

import java.io.IOException;
import java.util.List;

public class PlaylistManager {

    private final IPlaylistDataAccess playlistDAO;

    public PlaylistManager() throws IOException {
        playlistDAO = new PlaylistDAO_DB();
    }

    public Playlist createNewPlaylist(Playlist newPlaylist) throws Exception {
        return playlistDAO.createPlaylist(newPlaylist);
    }

    public List<Playlist> getAllPlaylist() throws Exception {
        return playlistDAO.getAllPlaylists();
    }

    public void updatePlaylist(Playlist selectedPlaylist) throws Exception {
        playlistDAO.updatePlaylist(selectedPlaylist);
    }

    public void deletePlaylist(Playlist selectedPlaylist) throws Exception {
        playlistDAO.deletePlaylist(selectedPlaylist);
    }
}
