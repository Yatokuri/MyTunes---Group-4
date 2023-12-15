/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BLL;

import easv.MyTunes.BE.Playlist;
import easv.MyTunes.DAL.IPlaylistDataAccess;
import easv.MyTunes.DAL.db.PlaylistDAO_DB;
import easv.MyTunes.GUI.Model.PlaylistModel;

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

    public Playlist getPlaylistById(int plId) {
        for (Playlist pl : PlaylistModel.getObservablePlaylists()) {
            if (pl.getId() == plId) {
                return pl;
            }
        }
        return null;
    }
}
