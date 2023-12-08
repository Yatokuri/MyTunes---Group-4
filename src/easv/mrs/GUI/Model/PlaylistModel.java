/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.GUI.Model;

import easv.mrs.BE.Playlist;
import easv.mrs.BLL.PlaylistManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlaylistModel {

    private static ObservableList<Playlist> playlistsToBeViewed = null;

    private final PlaylistManager playlistManager;

    public PlaylistModel() throws Exception {
        playlistManager = new PlaylistManager();
        playlistsToBeViewed = FXCollections.observableArrayList();
        playlistsToBeViewed.addAll(playlistManager.getAllPlaylist());
    }

    public void createNewPlaylist(Playlist newPlaylist) throws Exception {
        Playlist p = playlistManager.createNewPlaylist(newPlaylist);playlistsToBeViewed.add(p); // update list
    }

    public static ObservableList<Playlist> getObservablePlaylists() {return playlistsToBeViewed;}

    public void updatePlaylist(Playlist updatedPlaylist) throws Exception {
        // update song in DAL layer (through the layers)
        playlistManager.updatePlaylist(updatedPlaylist);

        // update observable list (and UI)
        Playlist p = playlistsToBeViewed.get(playlistsToBeViewed.indexOf(updatedPlaylist));
        p.setPlaylistName(updatedPlaylist.getPlaylistName());
    }

    public void deletePlaylist(Playlist selectedPlaylist) throws Exception {
        // delete song in DAL layer (through the layers)
        playlistManager.deletePlaylist(selectedPlaylist);

        // remove from observable list (and UI)
        playlistsToBeViewed.remove(selectedPlaylist);
    }
}