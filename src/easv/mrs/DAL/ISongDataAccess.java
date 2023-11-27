package easv.mrs.DAL;

import easv.mrs.BE.Song;

import java.util.List;

public interface ISongDataAccess {

    public List<Song> getAllSongs() throws Exception;

    public Song createSong(Song song) throws Exception;

    public void updateSong(Song song) throws Exception;

    public void deleteSong(Song song) throws Exception;

}
