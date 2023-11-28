package easv.mrs.GUI.Model;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.PlaylistManager;
import easv.mrs.BLL.SongManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class PlaylistModel {

    private final ObservableList<Playlist> playlistsToBeViewed;
    private final ObservableList<Song> playlistSongsToBeViewed;

    private final PlaylistManager playlistManager;
    private final SongManager songManager;

    public PlaylistModel() throws Exception {
        playlistManager = new PlaylistManager();
        songManager = new SongManager();
        playlistsToBeViewed = FXCollections.observableArrayList();
        playlistsToBeViewed.addAll(playlistManager.getAllPlaylist());

        playlistSongsToBeViewed = FXCollections.observableArrayList();
        for (Playlist p: playlistsToBeViewed) {
            playlistSongsToBeViewed.addAll(songManager.getAllSongsPlaylist(p));
        }

    }

    public void playlistSongs(Playlist playlist) throws Exception {
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(songManager.getAllSongsPlaylist(playlist));

    }


    public ObservableList<Song> getObservablePlaylistsSong() {return playlistSongsToBeViewed;}

    public ObservableList<Playlist> getObservablePlaylists() {return playlistsToBeViewed;}

    public void searchPlaylist(String query) throws Exception {
        List<Playlist> searchResults = playlistManager.searchPlaylists(query);
        playlistsToBeViewed.clear();
        playlistsToBeViewed.addAll(searchResults);
    }

    public void createNewPlaylist(Playlist newPlaylist) throws Exception {
        Playlist p = playlistManager.createNewPlaylist(newPlaylist);
       playlistsToBeViewed.add(p); // update list
    }

    public void updatePlaylist(Playlist updatedPlaylist) throws Exception {
        // update song in DAL layer (through the layers)
        playlistManager.updatePlaylist(updatedPlaylist);

        // update observable list (and UI)
        Playlist p = playlistsToBeViewed.get(playlistsToBeViewed.indexOf(updatedPlaylist));
        p.setPlaylistName(updatedPlaylist.getPlaylistName());
    }


    public void deleteSong(Playlist selectedPlaylist) throws Exception {
        // delete song in DAL layer (through the layers)
        playlistManager.deletePlaylist(selectedPlaylist);

        // remove from observable list (and UI)
        playlistsToBeViewed.remove(selectedPlaylist);
    }
}
