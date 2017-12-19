package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jacquelyn Gboyor on 11/22/2017.
 */

public final class OtakuContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private OtakuContract(){

    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/inventory/ is a valid path for
     * looking at item data. content://com.example.android.inventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_ITEMS = "inventory";

    /**
     * Inner class that defines constant values for the Otaku database table.
     * Each entry in the table represents a single item.
     */
    public static abstract class OtakuEntry implements BaseColumns{

        // The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        //Declare Table name and column names
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_IMAGE = "Product_Image";
        public static final String COLUMN_PRODUCT_NAME = "Product_Name";
        public static final String COLUMN_PRODUCT_CATEGORY = "Product_Category";
        public static final String COLUMN_PRODUCT_PRICE = "Product_Price";
        public static final String COLUMN_PRODUCT_QUANTITY = "Product_Quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER = "Product_SupplierName";
        public static final String COLUMN_PRODUCT_SUPPLIERPHONE = "Product_SupplierPhone";
        public static final String COLUMN_PRODUCT_SUPPLIEREMAIL = "Product_SupplierEmail";

    }//End OtakuEntry
}//End class
