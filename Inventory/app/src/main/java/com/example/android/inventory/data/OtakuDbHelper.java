package com.example.android.inventory.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventory.data.OtakuContract.OtakuEntry;

/**
 * Created by Jacquelyn Gboyor on 11/22/2017.
 */

public class OtakuDbHelper extends SQLiteOpenHelper{

    //Name of Database
    private static final String DATABASE_NAME = "Otakushop.db";
    private static final int VERSION = 1;

    public OtakuDbHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create statement
        String SQL_CREATE_TABLE =  "CREATE TABLE " + OtakuEntry.TABLE_NAME+ " ("
                + OtakuEntry._ID + " INTEGER  PRIMARY KEY  AUTOINCREMENT, "
                + OtakuEntry.COLUMN_PRODUCT_IMAGE + "  BLOB  NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_NAME + " TEXT  UNIQUE  NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_PRICE + " REAL (7, 2) NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER  NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT  NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE + " TEXT  NOT NULL, "
                + OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL + " TEXT  NOT NULL" + ");";

        db.execSQL(SQL_CREATE_TABLE);
    }//End------------------------------

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}//End class
