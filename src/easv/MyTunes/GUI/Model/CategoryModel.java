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

    public void createNewCategory(Category newCategory) throws Exception {
        Category c = categoryManager.createNewCategory(newCategory);categoriesToBeViewed.add(c); // update list
    }

    public static ObservableList<Category> getObservableCategories() {return categoriesToBeViewed;}

    public void deleteCategory(Category selectedCategory) throws Exception {
        // delete song in DAL layer (through the layers)
        categoryManager.deleteCategory(selectedCategory);

        // remove from observable list (and UI)
        categoriesToBeViewed.remove(selectedCategory);
    }

}
