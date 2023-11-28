package easv.mrs.DAL;

import easv.mrs.BE.Playlist;

import java.util.List;

public interface IPlaylistDataAccess {

    public List<Playlist> getAllPlaylists() throws Exception;

    public Playlist createPlaylist(Playlist playlist) throws Exception;

    public void updatePlaylist(Playlist playlist) throws Exception;

    public void deletePlaylist(Playlist playlist) throws Exception;

}
