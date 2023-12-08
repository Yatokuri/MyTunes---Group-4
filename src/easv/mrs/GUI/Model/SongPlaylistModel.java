package easv.mrs.GUI.Model;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.SongPlaylistManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class SongPlaylistModel {
    private final SongPlaylistManager songPlaylistManager;
    private final ObservableList<Song> playlistSongsToBeViewed;

    public SongPlaylistModel() throws Exception {
        songPlaylistManager = new SongPlaylistManager();
        playlistSongsToBeViewed = FXCollections.observableArrayList();
        for (Playlist p: PlaylistModel.getObservablePlaylists()) {
            playlistSongsToBeViewed.addAll(songPlaylistManager.getAllSongsPlaylist(p));
        }
    }
    public void playlistSongs(Playlist playlist) throws Exception {
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(songPlaylistManager.getAllSongsPlaylist(playlist));
    }
    public boolean addSongToPlaylist(Song newsong, Playlist playlist) throws Exception {
        for (Song s : playlistSongsToBeViewed) {
            if (newsong.getId() == s.getId()) {
                return false; // Exit the method fast
            }
        }
        songPlaylistManager.addSongToPlaylist(newsong, playlist);
        playlistSongsToBeViewed.add(newsong); // update list
        return true;
    }

    public void updateSongInPlaylist (Song song, Song oldsong, Playlist playlist) throws Exception {
        songPlaylistManager.updateSongInPlaylist(song, oldsong, playlist);
    }

    public void deleteSongFromPlaylist (Song song, Playlist playlist) throws Exception {
        songPlaylistManager.deleteSongFromPlaylist(song , playlist);
        playlistSongsToBeViewed.clear();
        playlistSongsToBeViewed.addAll(songPlaylistManager.getAllSongsPlaylist(playlist));
    }

    public void deleteAllSongsFromPlaylist (Playlist playlist) throws Exception {
        songPlaylistManager.deleteAllSongsFromPlaylist(playlist);
    }
    public ObservableList<Song> getObservablePlaylistsSong() {return playlistSongsToBeViewed;}
}
