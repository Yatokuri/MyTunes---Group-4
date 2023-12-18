package easv.MyTunes.GUI.Model;

import easv.MyTunes.BE.Category;
import easv.MyTunes.BLL.CategoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CategoryModel {

    private static ObservableList<Category> categoriesToBeViewed = null;
    private final CategoryManager categoryManager;

    public CategoryModel() throws Exception {
        categoryManager = new CategoryManager();
        categoriesToBeViewed = FXCollections.observableArrayList();
        categoriesToBeViewed.addAll(categoryManager.getAllCategories());
    }

    public void createNewCategory(Category newCategory) throws Exception { // Sends a request to the database to add a new category
        Category c = categoryManager.createNewCategory(newCategory);categoriesToBeViewed.add(c); // update list
    }

    public static ObservableList<Category> getObservableCategories() {return categoriesToBeViewed;} // Returns the categories from the Database

    public void deleteCategory(Category selectedCategory) throws Exception { // Sends a request to the database to delete a category
        // delete song in DAL layer (through the layers)
        categoryManager.deleteCategory(selectedCategory);

        // remove from observable list (and UI)
        categoriesToBeViewed.remove(selectedCategory);
    }

}
