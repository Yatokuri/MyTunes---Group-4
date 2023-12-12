/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.mrs.BLL;

import easv.mrs.BE.Category;
import easv.mrs.DAL.db.Category_DB;

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

    public List<Category> getAllCategories() throws Exception {
        return categoryDAO.getAllCategories();
    }
    public void deleteCategory(Category selectedCategory) throws Exception {
        categoryDAO.deleteCategory(selectedCategory);
    }



}