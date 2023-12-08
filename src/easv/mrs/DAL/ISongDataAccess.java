/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.DAL;

import easv.mrs.BE.Song;

import java.util.List;

public interface ISongDataAccess {

    List<Song> getAllSongs() throws Exception;

    Song createSong(Song song) throws Exception;

    void updateSong(Song song) throws Exception;

    void deleteSong(Song song) throws Exception;

}
