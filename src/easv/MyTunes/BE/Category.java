/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.MyTunes.BE;

public class Category {
    private String songCategory;

    public Category(String songCategory){
        this.songCategory = songCategory;
    }
    public String getSongCategory() {
        return songCategory;
    }
    public void setSongCategory(String songCategory) {
        this.songCategory = songCategory;
    }


    @Override
    public String toString()
    {
        return songCategory;
    }
}
