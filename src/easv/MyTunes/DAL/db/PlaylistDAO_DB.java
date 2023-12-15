/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.DAL.db;

// Project imports
import easv.MyTunes.BE.Playlist;
import easv.MyTunes.DAL.IPlaylistDataAccess;

//Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO_DB implements IPlaylistDataAccess {

    private final MyDatabaseConnector databaseConnector;

    public PlaylistDAO_DB() throws IOException {
        databaseConnector = new MyDatabaseConnector();
    }

    public List<Playlist> getAllPlaylists() throws Exception {

        ArrayList<Playlist> allPlaylists = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Playlist;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {

                //Map DB row to Playlist object
                int id = rs.getInt("Id");
                String title = rs.getString("Name");
                int songCount = rs.getInt("SongCount");
                double songTotalTime = rs.getDouble("SongTotalTime");
                Playlist playlist = new Playlist(id, title, songCount, songTotalTime);
                allPlaylists.add(playlist);
            }
            return allPlaylists;

        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get playlists from database", ex);
        }
    }

    public Playlist createPlaylist(Playlist playlist) throws Exception {

        // SQL command
        String sql = "INSERT INTO dbo.Playlist (Name) VALUES (?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, playlist.getPlaylistName());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create playlist object and send up the layers
            Playlist createdPlaylist;
            createdPlaylist = new Playlist(id, playlist.getPlaylistName(),playlist.getSongCount(),playlist.getSongTotalTime());

            return createdPlaylist;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create playlist", ex);
        }

    }

    public void updatePlaylist(Playlist playlist) throws Exception {

        // SQL command
        String sql = "UPDATE dbo.Playlist SET Name = ? WHERE ID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, playlist.getPlaylistName());
            stmt.setInt(2, playlist.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not update playlist", ex);
        }
    }

    public void deletePlaylist(Playlist playlist) throws Exception {
        // SQL command
        String  sql = "DELETE FROM dbo.PlaylistSongs WHERE PlaylistId = ?;";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))

        {
            // Bind parameters
            stmt.setInt(1, playlist.getId());
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete playlist", ex);
        }


        // SQL command
        sql = "DELETE FROM dbo.Playlist WHERE ID = ?;";


        // DELETE FROM dbo.PlaylistSongs WHERE PlaylistId = ?;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setInt(1, playlist.getId());
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
