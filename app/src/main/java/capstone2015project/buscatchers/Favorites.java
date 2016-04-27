package capstone2015project.buscatchers;

/**
 * Created by Waqar on 26-Apr-16.
 *
 */
public class Favorites extends AbstractFavorites {
    public Favorites(String favorite) {
        this.favorite = favorite;
    }

    @Override
    public String getFavorite() {
        return favorite;
    }

    @Override
    public boolean isNil() {
        return false;
    }
}
