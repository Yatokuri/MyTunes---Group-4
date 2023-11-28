package easv.mrs.BLL.util;

import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSearcher {


    public List<Playlist> search(List<Playlist> searchBase, String query) {
        List<Playlist> searchResult = new ArrayList<>();

        for (Playlist playlist : searchBase) {
            if(compareToSongTitle(query, playlist) || compareToSongYear(query, playlist))
            {
                searchResult.add(playlist);
            }
        }

        return searchResult;
    }
    private boolean compareToSongYear(String query, Playlist playlist) {
        return Integer.toString(playlist.getId()).contains(query);
    }

    private boolean compareToSongTitle(String query, Playlist playlist) {
        return playlist.getPlaylistName().toLowerCase().contains(query.toLowerCase());
    }

}
