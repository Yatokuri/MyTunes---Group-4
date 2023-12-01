package easv.mrs.GUI.Model;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.PlaylistManager;
import easv.mrs.BLL.SongManager;
import easv.mrs.GUI.Controller.MediaPlayerViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlaylistModel {

    private final ObservableList<Playlist> playlistsToBeViewed;
    private final ObservableList<Song> playlistSongsToBeViewed;

    private final PlaylistManager playlistManager;
    private MediaPlayerViewController mediaPlayerViewController;
    private final SongManager songManager;

    public PlaylistModel() throws Exception {
        playlistManager = new PlaylistManager();
        songManager = new SongManager();
        playlistsToBeViewed = FXCollections.observableArrayList();
        playlistsToBeViewed.addAll(playlistManager.getAllPlaylist());
        mediaPlayerViewController = MediaPlayerViewController.getInstance();
        playlistSongsToBeViewed = FXCollections.observableArrayList();
        for (Playlist p: playlistsToBeViewed) {
            playlistSongsToBeViewed.addAll(songManager.getAllSongsPlaylist(p));
        }

    }

    public ObservableList<Playlist> updatePlaylistList() throws Exception {
        playlistsToBeViewed.addAll(playlistManager.getAllPlaylist());
        return playlistsToBeViewed;
    }
    public void playlistSongs(Playlist playlist) throws Exception {
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(songManager.getAllSongsPlaylist(playlist));

    }




    public boolean addSongToPlaylist(Song newsong, Playlist playlist) throws Exception {
        for (Song s : playlistSongsToBeViewed) {
            if (newsong.getId() == s.getId()) {

                return false; // Exit the method fast
            }
        }
        songManager.addSongToPlaylist(newsong, playlist);
        playlistSongsToBeViewed.add(newsong); // update list
        return true;
    }

    public void deleteSongFromPlaylist (Song song, Playlist playlist) throws Exception {
        songManager.deleteSongFromPlaylist(song , playlist);
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(songManager.getAllSongsPlaylist(playlist));
    }



    public ObservableList<Song> getObservablePlaylistsSong() {return playlistSongsToBeViewed;}

    public ObservableList<Playlist> getObservablePlaylists() {return playlistsToBeViewed;}

    public void createNewPlaylist(Playlist newPlaylist) throws Exception {
        Playlist p = playlistManager.createNewPlaylist(newPlaylist);playlistsToBeViewed.add(p); // update list
    }

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
