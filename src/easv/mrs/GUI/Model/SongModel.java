package easv.mrs.GUI.Model;


import easv.mrs.BE.Song;
import easv.mrs.BLL.SongManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SongModel {

    private ObservableList<Song> songsToBeViewed;
    
    private final SongManager songManager;

    public SongModel() throws Exception {
        songManager = new SongManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(songManager.getAllSongs());
    }


    public ObservableList<Song> updateSongList() throws Exception {
        songsToBeViewed.addAll(songManager.getAllSongs());
        return songsToBeViewed;
    }
    public ObservableList<Song> getObservableSongs() { return songsToBeViewed; }
    public Song createNewSong(Song newSong) throws Exception {
        Song s = songManager.createNewSong(newSong);
        songsToBeViewed.add(s); // update list
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());
        return s;
    }


    public void updateSong(Song updatedSong) throws Exception {
        // update song in DAL layer (through the layers)
        songManager.updateSong(updatedSong);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(songManager.getAllSongs());

   // update observable list (and UI)

    }


    public void deleteSong(Song selectedSong) throws Exception {
        // delete song in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);

        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }
}
