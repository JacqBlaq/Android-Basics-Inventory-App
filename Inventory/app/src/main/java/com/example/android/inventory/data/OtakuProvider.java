package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Jacquelyn Gboyor on 11/29/2017.
 */

public class OtakuProvider extends ContentProvider {

    //Tage for log messages
    public static final String LOG_TAG = OtakuProvider.class.getSimpleName();
    private static OtakuDbHelper helper; //Databse helper
    static long id;

    /** URI matcher code for the content URI for the inventory table */
    private static final int ITEMs = 100;

    /** URI matcher code for the content URI for a single item in the inventory table */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sUriMatcher.addURI(OtakuContract.CONTENT_AUTHORITY, OtakuContract.PATH_ITEMS, ITEMs);
        sUriMatcher.addURI(OtakuContract.CONTENT_AUTHORITY, OtakuContract.PATH_ITEMS + "/#", ITEM_ID);
    }//End
    /*************************************************************************************/

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        helper = new OtakuDbHelper(getContext());
        return true;
    }//ENd onCreate
    /*************************************************************************************/

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,  String[] selectionArgs, String sortOrder) {

        //Get readable database
        SQLiteDatabase db = helper.getReadableDatabase();

        //Cursor that will hold the result of the query
        Cursor cursor;

        //Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMs:
                cursor = db.query(
                        OtakuContract.OtakuEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEM_ID:
                selection = OtakuContract.OtakuEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(
                        OtakuContract.OtakuEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);//*******
        return cursor;
    }//End query
    /*************************************************************************************/

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMs:
                return OtakuContract.OtakuEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return OtakuContract.OtakuEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }//End getType
    /*************************************************************************************/

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMs:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }//End insert
    /*************************************************************************************/

    /**
     * Insert an into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {

        //Get wriatable database
        SQLiteDatabase db = helper.getWritableDatabase();

        StringBuilder errorMessages = new StringBuilder();
        byte[] img = values.getAsByteArray(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE);
        String name = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME);
        String price = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE);
        String category = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY);
        Integer quantity = values.getAsInteger(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY);
        String supplierName = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER);
        String supplierPhone = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE);
        String supplierEmail = values.getAsString(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL);

        id = db.insert(OtakuContract.OtakuEntry.TABLE_NAME,null, values);
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);//******

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

    }//End insertItem
    /*************************************************************************************/

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        //Get writable database
        SQLiteDatabase db = helper.getWritableDatabase();
        int rowsDeleted; //Track the number of rows that were deleted
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMs:
                rowsDeleted = db.delete(
                        OtakuContract.OtakuEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case ITEM_ID:
                selection = OtakuContract.OtakuEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(OtakuContract.OtakuEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported " + uri);
        }//-------------------

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }//-------------------

        //return the number of rows deleted
        return rowsDeleted;
    }//End Delete
    /*************************************************************************************/

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMs:
                return update(
                        uri,
                        values,
                        selection,
                        selectionArgs);
            case ITEM_ID:
                selection = OtakuContract.OtakuEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateItem(
                        uri,
                        values,
                        selection,
                        selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }//End switch
    }//End update
    /*************************************************************************************/

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.size() == 0){
            return  0;
        }
        //Get writable database
        SQLiteDatabase db = helper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(OtakuContract.OtakuEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }//End updateItem
    /*************************************************************************************/

    public static void sellItem(long currentId, int quant){
        //This method is called from MainActivity and passes the current ID and quantity of item
        //So when sale button is clicked in listView, it subtracts the quantity by 1
        //Get writable database
        SQLiteDatabase db = helper.getWritableDatabase();

        int newQant = 0; //Variable to hold updated quantity
        if (quant > 0){ // only subtract from quantity if it is greater than 0
            newQant = quant - 1;
        }
        //Create content values object
        ContentValues val = new ContentValues();

        //Input new quantity value into column
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, newQant);

        String selection = OtakuContract.OtakuEntry._ID + "=?"; //row ID
        String[] selectionArgs = new String[] {String.valueOf(currentId)}; //row URI (Universal Resource Identifier)

        //Update database
        id = db.update(OtakuContract.OtakuEntry.TABLE_NAME, val, selection, selectionArgs);

        if (id == -1){
            //Log message if update fails
            Log.e(LOG_TAG, "Failed to update row for " + currentId);
        }
    }//End sellItem
    /*************************************************************************************/

    public static Cursor updateQuantity() {
        //writable database
        SQLiteDatabase db = helper.getReadableDatabase();

        //Defined projection that contains all columns of Otakushop table
        String [] projection = {
                OtakuContract.OtakuEntry._ID,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL
        };

        Cursor cursor = db.query(
                OtakuContract.OtakuEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }//End updateQuantity

}//End class
