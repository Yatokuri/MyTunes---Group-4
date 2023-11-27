package easv.mrs.DAL.db;

// Project imports
import easv.mrs.BE.Song;
import easv.mrs.DAL.ISongDataAccess;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO_DB implements ISongDataAccess {

    private MyDatabaseConnector databaseConnector;

    public SongDAO_DB() throws IOException {
        databaseConnector = new MyDatabaseConnector();
    }

    public List<Song> getAllSongs() throws Exception {

        ArrayList<Song> allSongs = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Songs;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {

                //Map DB row to Movie object
                int id = rs.getInt("SongId");
                String title = rs.getString("SongName");
                String artist = rs.getString("SongArtist");
                int year = rs.getInt("SongYear");
                String songPath = rs.getString("SongFilepath");
                Song song = new Song(id, year, title, artist, songPath);
                allSongs.add(song);
            }
            return allSongs;

        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get movies from database", ex);
        }


        //TODO Do this
        //throw new UnsupportedOperationException();
    }

    public Song createSong(Song song) throws Exception {

        // SQL command
        String sql = "INSERT INTO dbo.Songs (SongName, SongArtist, SongYear, SongFilepath) VALUES (?,?,?,?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setInt(3, song.getYear());
            stmt.setString(4, song.getSongPath());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create movie object and send up the layers
            Song createdSong = new Song(id, song.getYear(), song.getTitle(), song.getArtist(), song.getSongPath());

            return createdSong;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create movie", ex);
        }

    }

    public void updateSong(Song song) throws Exception {

        // SQL command
        String sql = "UPDATE dbo.Songs SET SongName = ?, SongArtist = ?, SongYear = ? WHERE SongID = ?";

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

    public void deleteSong(Song song) throws Exception {
        // SQL command
        String sql = "DELETE FROM dbo.Songs WHERE SongID = ?;";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setInt(1, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete movie", ex);
        }
    }

    public List<Song> searchSongs(String query) throws Exception {

        //TODO Do this
        throw new UnsupportedOperationException();
    }

}
