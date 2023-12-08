/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.DAL;

import easv.mrs.BE.Playlist;

import java.util.List;

public interface IPlaylistDataAccess {

    List<Playlist> getAllPlaylists() throws Exception;

    Playlist createPlaylist(Playlist playlist) throws Exception;

    void updatePlaylist(Playlist playlist) throws Exception;

    void deletePlaylist(Playlist playlist) throws Exception;

}
