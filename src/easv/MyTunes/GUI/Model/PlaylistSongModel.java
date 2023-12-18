package easv.MyTunes.GUI.Model;

import easv.MyTunes.BE.Playlist;
import easv.MyTunes.BE.Song;
import easv.MyTunes.BLL.PlaylistSongManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class PlaylistSongModel {
    private final PlaylistSongManager playlistSongManager;
    private final ObservableList<Song> playlistSongsToBeViewed;

    public PlaylistSongModel() throws Exception {
        playlistSongManager = new PlaylistSongManager();
        playlistSongsToBeViewed = FXCollections.observableArrayList();
        for (Playlist p: PlaylistModel.getObservablePlaylists()) {
            playlistSongsToBeViewed.addAll(playlistSongManager.getAllSongsPlaylist(p));
        }
    }
    public void playlistSongs(Playlist playlist) throws Exception { // changes the playlist you are viewing and inserts the relevant songs
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(playlistSongManager.getAllSongsPlaylist(playlist));
    }
    public boolean addSongToPlaylist(Song newsong, Playlist playlist) throws Exception { // Sends a request to the database to add a song to a playlist
        for (Song s : playlistSongsToBeViewed) {
            if (newsong.getId() == s.getId()) {
                return false; // Exit the method fast
            }
        }
        playlistSongManager.addSongToPlaylist(newsong, playlist);
        playlistSongsToBeViewed.add(newsong); // update list // Adds the new song to the playlist observable list
        return true;
    }

    public void updateSongInPlaylist (Song song, Song oldsong, Playlist playlist) throws Exception { // Sends a request to the database to update a song in a playlist
        playlistSongManager.updateSongInPlaylist(song, oldsong, playlist);
    }

    public void deleteSongFromPlaylist (Song song, Playlist playlist) throws Exception { // Sends a request to the database to delete a song from a playlist
        playlistSongManager.deleteSongFromPlaylist(song , playlist);
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(playlistSongManager.getAllSongsPlaylist(playlist)); // Updates the playlist observable list with the changes
    }

    public void deleteAllSongsFromPlaylist (Playlist playlist) throws Exception { // Sends a request to the database to empty the playlist of all songs
        playlistSongManager.deleteAllSongsFromPlaylist(playlist);
    }
    public ObservableList<Song> getObservablePlaylistsSong() {return playlistSongsToBeViewed;} // Returns the playlist
}
