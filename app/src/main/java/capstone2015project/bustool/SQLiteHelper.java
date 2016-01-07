package capstone2015project.bustool;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by jani on 8.12.2015.
 *
 * Contains methods to handle database;
 *
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE = "busstop.db";
    public static final String TABLE = "busstops";
    public static final String ID = "id";
    public static final String BUSSTOP_ID = "bs_id";
    public static final String BUSSTOP_NAME = "bs_nm";
    public static final String BUSSTOP_TAGS = "bs_tags";
    public static final String BUSSTOP_FAV = "bs_fav"; // true or false
    public static final String BUSSTOP_LAT = "bs_lat";
    public static final String BUSSTOP_LON = "bs_lon";
    public final ArrayList<String> BsIdList = new ArrayList<String>();

    public SQLiteHelper(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BUSSTOP_ID + " TEXT, " + BUSSTOP_NAME + " TEXT, " + BUSSTOP_TAGS + " TEXT, " + BUSSTOP_FAV + " INTEGER, " + BUSSTOP_LAT + " Decimal(9,6), " + BUSSTOP_LON + " Decimal(9,6))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist" + TABLE);
        onCreate(db);
    }

    public Cursor getData(String query)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( query, null );
    }

    public Boolean insertBS(String bs_id, String bs_name, double bs_lat, double bs_lon, String bs_fav){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUSSTOP_ID,bs_id);
        contentValues.put(BUSSTOP_NAME, bs_name);
        contentValues.put(BUSSTOP_LAT, bs_lat);
        contentValues.put(BUSSTOP_LON, bs_lon);
        contentValues.put(BUSSTOP_FAV, bs_fav);
        long result = db.insert(TABLE, null, contentValues);
        return result != -1;
    }

    public Cursor readBS(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+TABLE+" where id="+id+"", null );
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE);
    }

    public boolean updateBS (Integer id, String bs_name, double bs_lat, double bs_lon, String bs_fav) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUSSTOP_NAME, bs_name);
        contentValues.put(BUSSTOP_LAT, bs_lat);
        contentValues.put(BUSSTOP_LON, bs_lon);
        contentValues.put(BUSSTOP_FAV, bs_fav);
        db.update(TABLE, contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public boolean DeleteAll () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE " + TABLE);
        onCreate(db);
        return true;
    }

    public Integer deleteBS (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllBSs()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + TABLE, null);
        res.moveToFirst();
        BsIdList.clear();
        while(!res.isAfterLast()){
            BsIdList.add(res.getString(res.getColumnIndex(BUSSTOP_ID)));
            array_list.add(res.getString(res.getColumnIndex(BUSSTOP_ID))+"\t\t"+res.getString(res.getColumnIndex(BUSSTOP_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getQuery(String qString)
    {

        ArrayList<String> array_list = new ArrayList<String>();
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("PRAGMA case_sensitive_like=OFF;");
        Cursor res =  db.rawQuery(qString, null);
        res.moveToFirst();
        BsIdList.clear();
        while(!res.isAfterLast()){
            BsIdList.add(res.getString(res.getColumnIndex(BUSSTOP_ID)));
            array_list.add(res.getString(res.getColumnIndex(BUSSTOP_ID))+"\t\t"+res.getString(res.getColumnIndex(BUSSTOP_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public Boolean addFav(String BsToAdd){
        SQLiteDatabase db = this.getReadableDatabase();
        // Get Id to Update
        Cursor res =  db.rawQuery("SELECT * FROM " + TABLE + " WHERE bs_id LIKE '" + BsToAdd + "' ", null);
        res.moveToFirst();
        String id = res.getString(res.getColumnIndex(ID));
        // Update Bs_Fav
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUSSTOP_FAV, 1);
        db.update(TABLE, contentValues, "id = ? ", new String[]{id});
        return true;
    }

    public Boolean delFav(String BsToDel){
        SQLiteDatabase db = this.getReadableDatabase();
        // Get Id to Update
        Cursor res =  db.rawQuery("SELECT * FROM "+TABLE+" WHERE bs_id LIKE '"+BsToDel+"' ", null);
        res.moveToFirst();String id = res.getString(res.getColumnIndex(ID));
        // Update Bs_Fav
        ContentValues contentValues = new ContentValues();
        contentValues.put(BUSSTOP_FAV, 0);
        db.update(TABLE, contentValues, "id = ? ", new String[]{id});
        return true;
    }
}