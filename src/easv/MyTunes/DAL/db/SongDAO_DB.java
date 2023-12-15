/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.DAL.db;

// Project imports
import easv.MyTunes.BE.Song;
import easv.MyTunes.DAL.ISongDataAccess;

// Java imports
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO_DB implements ISongDataAccess {

    private final MyDatabaseConnector databaseConnector;
    ArrayList<Song> allSongs;

    public SongDAO_DB() throws Exception {
        databaseConnector = new MyDatabaseConnector();
        getAllSongs();
    }

    public List<Song> getSongsArray() { return allSongs; }


    public List<Song> getAllSongs() throws Exception {

        allSongs = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Songs;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to Song object
                int id = rs.getInt("SongId");
                String title = rs.getString("SongName");
                String artist = rs.getString("SongArtist");
                int year = rs.getInt("SongYear");
                String songPath = rs.getString("SongFilepath");
                double songLength = rs.getDouble("SongLength");
                String songCategory = rs.getString("SongCategory");
                Song song = new Song(id, year, title, artist, songPath, songLength, songCategory);
                allSongs.add(song);
            }
            return allSongs;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get songs from database", ex);
        }
    }

    public Song createSong(Song song) throws Exception {

        // SQL command
        String sql = "INSERT INTO dbo.Songs (SongName, SongArtist, SongYear, SongFilepath, songLength, songCategory) VALUES (?,?,?,?,?,?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setInt(3, song.getYear());
            stmt.setString(4, song.getSongPath());
            stmt.setDouble(5,song.getSongLength());
            stmt.setString(6,song.getSongCategory());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Song object and send up the layers

            Song newSong = new Song(id, song.getYear(), song.getTitle(), song.getArtist(), song.getSongPath(), song.getSongLength(), song.getSongCategory());
            return newSong;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create Song", ex);
        }
    }

    public void updateSong(Song song) throws Exception {

        // SQL command
        String sql = "UPDATE dbo.Songs SET SongName = ?, SongArtist = ?, SongYear = ?, SongFilepath = ?, SongLength = ?, SongCategory = ? WHERE SongID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setInt(3, song.getYear());
            stmt.setString(4,song.getSongPath());
            stmt.setBigDecimal(5, BigDecimal.valueOf(song.getSongLength()));
            stmt.setString(6,song.getSongCategory());
            stmt.setInt(7, song.getId());

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

    public void deleteSong(Song song) throws Exception {

        // SQL command
        String sqlSongs = "DELETE FROM dbo.Songs WHERE SongID = ?;";
        String sqlPlaylistSongs = "DELETE FROM dbo.PlaylistSongs WHERE SongID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSongs);
             PreparedStatement stmt2 = conn.prepareStatement(sqlPlaylistSongs)) {
            // Bind parameters
            stmt.setInt(1, song.getId());
            stmt2.setInt(1, song.getId());

            // Run the specified SQL statement
            stmt2.executeUpdate();
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete Song", ex);
        }
    }
}
