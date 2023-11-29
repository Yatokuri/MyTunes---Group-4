package easv.mrs.DAL.db;

// Project imports
import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;
import easv.mrs.BLL.SongManager;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistSongDAO_DB {

    private MyDatabaseConnector databaseConnector;
    private SongDAO_DB songDAO_db;
    private SongManager songManager;

    public PlaylistSongDAO_DB() throws IOException {
        databaseConnector = new MyDatabaseConnector();
        songDAO_db = new SongDAO_DB();
    }

    public List<Song> getAllSongsPlaylist(Playlist playlist) throws Exception {

        playlist.setSongTotalTime(0);
        playlist.setSongCount(0);

        ArrayList<Song> allSongsInPlaylist = new ArrayList<>();

        String sql = "SELECT Songs.SongId, Songs.SongName FROM Songs\n" +
                    "JOIN PlaylistSongs S1 ON Songs.SongId = S1.SongId\n" +
                    "WHERE S1.PlayListId =" + playlist.getId() + " ";

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set



            while (rs.next()) {

                //Map DB row to Movie object
                int id = rs.getInt("SongId");


                for (Song s  : songDAO_db.getSongsArray())    {
                    if (s.getId() == id)    {
                        playlist.setSongCount(playlist.getSongCount() + 1);
                        playlist.setSongTotalTime(playlist.getSongTotalTime() + s.getSongLength());
                        allSongsInPlaylist.add(s);
                    }
                }
            }
            return allSongsInPlaylist;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get songs in playlist from database", ex);
        }
    }

    public void addSongToPlaylist(Song song, Playlist playlist) throws Exception {

        // SQL command
        String sql = "INSERT INTO dbo.PlaylistSongs (SongId, PlaylistId) VALUES (?,?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setInt(1, song.getId());
            stmt.setInt(2, playlist.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();


            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();


            // Create movie object and send up the layers
           // Song createdSong = new Song(id, song.getYear(), song.getTitle(), song.getArtist(), song.getSongPath(), song.getSongLength());

           // return createdSong;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not add song to playlist", ex);
        }

    }

    public void updateSong(Song song) throws Exception {

        // SQL command
        String sql = "UPDATE dbo.PlaylistSongs SET SongName = ?, SongArtist = ?, SongYear = ? WHERE SongID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setInt(3, song.getYear());
            stmt.setInt(4, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not update song", ex);
        }
    }

    public void deleteSongFromPlaylist(Song song, Playlist playlist) throws Exception {
        // SQL command
        String sql = "DELETE FROM dbo.PlaylistSongs WHERE SongID = ? AND PlaylistId = ?;";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setInt(1, song.getId());
            stmt.setInt(2, playlist.getId());
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete playlist", ex);
        }
    }
}
