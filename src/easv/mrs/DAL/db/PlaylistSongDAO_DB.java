package easv.mrs.DAL.db;

// Project imports
import easv.mrs.BE.Playlist;
import easv.mrs.BE.Song;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistSongDAO_DB {

    private final MyDatabaseConnector databaseConnector;
    private final SongDAO_DB songDAO_db;

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
                    "WHERE S1.PlayListId =" + playlist.getId() + "\n" +
                    "ORDER BY S1.playlistorder ASC";
        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to playlist object
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

        System.out.println("Jeg vil tilføje " + song.getTitle() + "til "  + playlist.getPlaylistName());

        // SQL command
        String sql = "INSERT INTO dbo.PlaylistSongs (SongId, PlaylistId, PlayListOrder) VALUES (?,?,?);";

        String sql2 = "SELECT MAX(PlayListOrder) AS highest_value FROM dbo.PlaylistSongs  WHERE PlayListId = "+ playlist.getId() + ";";

        System.out.println(sql2);

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             Statement stmt2 = conn.createStatement())
        {
            ResultSet rs2 = stmt2.executeQuery(sql2);

            int nextIdNumber = 1;
            while (rs2.next()) {
                //Map DB row to playlist object
                nextIdNumber = rs2.getInt("highest_value");
            }

            // Bind parameters
            stmt.setInt(1, song.getId());
            stmt.setInt(2, playlist.getId());
            stmt.setInt(3, nextIdNumber+1);

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not add song to playlist", ex);
        }
    }

    public void updateSongInPlaylist(Song song, Song oldSong, Playlist playlist) throws Exception {
        // SQL command
        String sql = "UPDATE dbo.PlaylistSongs SET PlayListOrder = ? WHERE SongId = ? AND PlaylistId = ?";
        String sqlOldSong = "SELECT PlayListOrder FROM dbo.PlaylistSongs WHERE SongId = "+ oldSong.getId() + " AND PlaylistId = "+ playlist.getId();
        String sqlNewSong = "SELECT PlaylistOrder FROM dbo.PlaylistSongs WHERE SongId = "+ song.getId() + "AND PlaylistId = "+ playlist.getId();
        try (Connection conn = databaseConnector.getConnection();
             Statement stmt2 = conn.createStatement();
             Statement stmt3 = conn.createStatement();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            ResultSet rs = stmt2.executeQuery(sqlOldSong);
            ResultSet rs2 = stmt3.executeQuery(sqlNewSong);
            int playOrderOld = 1;
            int playOrderNewSong = 1;
            while (rs.next()) {
                //Map DB row to playlist object
                playOrderOld = rs.getInt("PlayListOrder");
            }
            while (rs2.next()){
                playOrderNewSong = rs2.getInt("PlayListOrder");
            }

            // Bind parameters
            stmt.setInt(1, playOrderOld);
            stmt.setInt(2, song.getId());
            stmt.setInt(3, playlist.getId());

            if (playOrderNewSong < playOrderOld) {
                String sqlUpdatePlaylist = "UPDATE dbo.PlaylistSongs SET PlaylistOrder = PlaylistOrder - 1 WHERE PlaylistId =? AND PlaylistOrder <= ? AND PlaylistOrder >=?";
                try (PreparedStatement stmt4 = conn.prepareStatement(sqlUpdatePlaylist)) {
                    stmt4.setInt(1, playlist.getId());
                    stmt4.setInt(2, playOrderOld);
                    stmt4.setInt(3, playOrderNewSong);
                    stmt4.executeUpdate();
                }
            }
            if (playOrderNewSong > playOrderOld){
                String sqlUpdatePlaylist = "UPDATE dbo.PlaylistSongs SET PlaylistOrder = PlaylistOrder + 1 WHERE PlaylistId = ? AND PlaylistOrder >= ? AND PlaylistOrder <=?";
                try (PreparedStatement stmt4 = conn.prepareStatement(sqlUpdatePlaylist)) {
                    stmt4.setInt(1, playlist.getId());
                    stmt4.setInt(2, playOrderOld);
                    stmt4.setInt(3, playOrderNewSong);
                    stmt4.executeUpdate();
                }
            }
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
        //When we delete a song we also need to change there order

        // SQL command
        String sqlSongsPlayOrder = "SELECT PlayListOrder FROM dbo.PlaylistSongs WHERE SongId = ? AND PlaylistId = ?";
        String sqlDeleteSong = "DELETE FROM dbo.PlaylistSongs WHERE SongID = ? AND PlaylistId = ?;";

        try (Connection conn = databaseConnector.getConnection();
            PreparedStatement stmtDeleteSong = conn.prepareStatement(sqlDeleteSong);
             PreparedStatement  stmtSongsPlayOrder = conn.prepareStatement(sqlSongsPlayOrder)) {
            // Bind parameters
            stmtDeleteSong.setInt(1, song.getId());
            stmtDeleteSong.setInt(2, playlist.getId());
            stmtSongsPlayOrder.setInt(1, song.getId());
            stmtSongsPlayOrder.setInt(2, playlist.getId());
            // Run the specified SQL statement
            ResultSet rs = stmtSongsPlayOrder.executeQuery();
            stmtDeleteSong.executeUpdate(); //We delete song after cause otherwise we cannot find the right playlist order

            int playOrder = -1; //Default if no one is found
            while (rs.next()) {
                //Map DB row to playlist object
                playOrder = rs.getInt("PlayListOrder");
            }
            if (playOrder > -1) {
                String sqlUpdatePlayOrder = "UPDATE dbo.PlaylistSongs SET PlaylistOrder = PlaylistOrder - 1 WHERE PlaylistId = ? AND PlaylistOrder >= ?";
                try (PreparedStatement stmt3 = conn.prepareStatement(sqlUpdatePlayOrder)) {
                    stmt3.setInt(1, playlist.getId());
                    stmt3.setInt(2, playOrder);
                    stmt3.executeUpdate();
                }
            }
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete playlist", ex);
        }
    }

    public void deleteAllSongsFromPlaylist(Playlist playlist) throws Exception {
        // SQL command
        String sql = "DELETE FROM dbo.PlaylistSongs WHERE PlaylistId = ?;";

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