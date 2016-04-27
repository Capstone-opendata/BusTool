package capstone2015project.buscatchers;

import android.app.Application;
import android.content.Context;

/**
 * Created by Waqar on 26-Apr-16.
 * implements factory pattern
 */
public class FavoritesFactory extends Application{
    //get application context
    protected Context mContext;
    public FavoritesFactory(Context context) {
        mContext = context;
    }
    //fetch favorites
    SQLiteHelper BsDb = SQLiteHelper.getInstance(mContext);
    String[] favoriteStops = BsDb.favoriteStops();
    public AbstractFavorites getFavorite(String favorite){
        for (int i = 0; i < favoriteStops.length; i++) {
            if (favoriteStops[i].equalsIgnoreCase(favorite)){
                return new Favorites(favorite);
            }
        }
        return new NullFavorites();
    }
}