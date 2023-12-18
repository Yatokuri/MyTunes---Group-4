/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.GUI.Model;

import easv.MyTunes.BE.Playlist;
import easv.MyTunes.BLL.PlaylistManager;

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

    public void createNewPlaylist(Playlist newPlaylist) throws Exception { // Sends a request to the database to create a new playlist
        Playlist p = playlistManager.createNewPlaylist(newPlaylist);playlistsToBeViewed.add(p); // update list
    }

    public static ObservableList<Playlist> getObservablePlaylists() {return playlistsToBeViewed;} // Returns the playlists

    public void updatePlaylist(Playlist updatedPlaylist) throws Exception { // Sends a request to the database to update a playlist
        // update song in DAL layer (through the layers)
        playlistManager.updatePlaylist(updatedPlaylist);

        // update observable list (and UI)
        Playlist p = playlistsToBeViewed.get(playlistsToBeViewed.indexOf(updatedPlaylist));
        p.setPlaylistName(updatedPlaylist.getPlaylistName());
    }

    public void deletePlaylist(Playlist selectedPlaylist) throws Exception { // Sends a request to the database to delete a playlist
        // delete song in DAL layer (through the layers)
        playlistManager.deletePlaylist(selectedPlaylist);

        // remove from observable list (and UI)
        playlistsToBeViewed.remove(selectedPlaylist);
    }
    public Playlist getPlaylistById(int plId){ // Returns a Playlist based on its id
        return playlistManager.getPlaylistById(plId);
    }
}