/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.DAL;

import easv.MyTunes.BE.Playlist;

import java.util.List;

public interface IPlaylistDataAccess {

    List<Playlist> getAllPlaylists() throws Exception;

    Playlist createPlaylist(Playlist playlist) throws Exception;

    void updatePlaylist(Playlist playlist) throws Exception;

    void deletePlaylist(Playlist playlist) throws Exception;

}
