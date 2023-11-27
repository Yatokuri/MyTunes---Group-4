package easv.mrs.BLL.util;

import easv.mrs.BE.Song;

import java.util.ArrayList;
import java.util.List;

public class SongSearcher {


    public List<Song> search(List<Song> searchBase, String query) {
        List<Song> searchResult = new ArrayList<>();

        for (Song song : searchBase) {
            if(compareToSongTitle(query, song) || compareToSongYear(query, song))
            {
                searchResult.add(song);
            }
        }

        return searchResult;
    }

    private boolean compareToSongYear(String query, Song song) {
        return Integer.toString(song.getYear()).contains(query);
    }

    private boolean compareToSongTitle(String query, Song song) {
        return song.getTitle().toLowerCase().contains(query.toLowerCase());
    }

}
