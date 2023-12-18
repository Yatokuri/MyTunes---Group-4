/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BLL;

import easv.MyTunes.BE.Category;
import easv.MyTunes.DAL.db.Category_DB;

import java.io.IOException;
import java.util.List;

public class CategoryManager {
    private final Category_DB categoryDAO;
    public CategoryManager() throws IOException {
        categoryDAO = new Category_DB();
    }

    public Category createNewCategory(Category newCategory) throws Exception {
        return categoryDAO.createCategory(newCategory);
    }

    public List<Category> getAllCategories() throws Exception { // Sends a query request
        return categoryDAO.getAllCategories();
    }
    public void deleteCategory(Category selectedCategory) throws Exception { // Sends a request to the database to delete the selected category
        categoryDAO.deleteCategory(selectedCategory);
    }



}
