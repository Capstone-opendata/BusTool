package capstone2015project.buscatchers;

/**
 * Created by Waqar on 26-Apr-16.
 */
public class NullFavorites extends AbstractFavorites{
    @Override
    public String getFavorite() {
        //not a favorite
        return "Not a favorite";
    }

    @Override
    public boolean isNil() {
        return true;
    }
}
