package easv.mrs.BLL.util;

import easv.mrs.BE.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSearcher {


    public List<Playlist> search(List<Playlist> searchBase, String query) {
        List<Playlist> searchResult = new ArrayList<>();

        for (Playlist playlist : searchBase) {
            if(compareToPlaylistTitle(query, playlist) || compareToPlaylistId(query, playlist))
            {
                searchResult.add(playlist);
            }
        }

        return searchResult;
    }
    private boolean compareToPlaylistId(String query, Playlist playlist) {
        return Integer.toString(playlist.getId()).contains(query);
    }

    private boolean compareToPlaylistTitle(String query, Playlist playlist) {
        return playlist.getPlaylistName().toLowerCase().contains(query.toLowerCase());
    }

}
