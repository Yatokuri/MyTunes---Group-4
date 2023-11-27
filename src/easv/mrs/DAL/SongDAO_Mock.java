package easv.mrs.DAL;

import easv.mrs.BE.Song;

import java.util.ArrayList;
import java.util.List;

public class SongDAO_Mock implements ISongDataAccess {

    private List<Song> allSongs;

    public SongDAO_Mock()
    {
        allSongs = new ArrayList<>();
        allSongs.add(new Song(1, 2020,"Trump - the movie", "Trump"));
        allSongs.add(new Song(1, 2024, "Trump - I did it again", "Trump"));
        allSongs.add(new Song(1, 2028,"Trump - The new dictator on the block", "Kesha"));

    }

    @Override
    public List<Song> getAllSongs() {
        return allSongs;
    }

    @Override
    public Song createSong(Song song) throws Exception {
        return null;
    }

    @Override
    public void updateSong(Song song) throws Exception {

    }

    @Override
    public void deleteSong(Song song) throws Exception {

    }

}
