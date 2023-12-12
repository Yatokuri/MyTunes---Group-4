/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.DAL.db;

import easv.mrs.BE.Category;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Category_DB {
    private final MyDatabaseConnector databaseConnector;

    public Category_DB() throws IOException {
        databaseConnector = new MyDatabaseConnector();
    }
    public List<Category> getAllCategories() throws Exception {

        ArrayList<Category> allCategories = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Category;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to Playlist object
                String songCategory = rs.getString("Category");
                Category category = new Category(songCategory);
                allCategories.add(category);
            }
            return allCategories;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get Categories from database", ex);
        }
    }

    public Category createCategory(Category category) throws Exception {

        // SQL command
        String sql = "INSERT INTO dbo.Category (Category) VALUES (?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, category.getSongCategory());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Create category object and send up the layers
            Category createdCategory;
            createdCategory = new Category(category.getSongCategory());
            return createdCategory;
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create Category", ex);
        }
    }
    public void deleteCategory(Category category) throws Exception {
        // SQL command
        String  sql = "DELETE FROM dbo.Category WHERE Category = ?";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))

        {
            // Bind parameters
            stmt.setString(1, category.getSongCategory());
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete Category", ex);
        }
    }
}
