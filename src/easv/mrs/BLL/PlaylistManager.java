package easv.mrs.BLL;

import easv.mrs.BE.Playlist;
import easv.mrs.BLL.util.PlaylistSearcher;
import easv.mrs.DAL.IPlaylistDataAccess;
import easv.mrs.DAL.db.PlaylistDAO_DB;

import java.io.IOException;
import java.util.List;

public class PlaylistManager {

    private PlaylistSearcher playlistSearcher = new PlaylistSearcher();
    private IPlaylistDataAccess playlistDAO;

    public PlaylistManager() throws IOException {
        playlistDAO = new PlaylistDAO_DB();
    }
    public List<Playlist> getAllPlaylist() throws Exception {
        return playlistDAO.getAllPlaylists();
    }

    public List<Playlist> searchPlaylists(String query) throws Exception {
        List<Playlist> allPlaylists = playlistDAO.getAllPlaylists();
        List<Playlist> searchResult = playlistSearcher.search(allPlaylists, query);
        return searchResult;
    }

    public Playlist createNewPlaylist(Playlist newPlaylist) throws Exception {
        return playlistDAO.createPlaylist(newPlaylist);
    }

    public void updatePlaylist(Playlist selectedPlaylist) throws Exception {
        playlistDAO.updatePlaylist(selectedPlaylist);
    }

    public void deletePlaylist(Playlist selectedPlaylist) throws Exception {
        playlistDAO.deletePlaylist(selectedPlaylist);
    }
}
