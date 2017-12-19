package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.android.inventory.data.OtakuContract;
import com.example.android.inventory.data.OtakuContract.OtakuEntry;
import com.example.android.inventory.data.OtakuCursorAdapter;
import com.example.android.inventory.data.OtakuDbHelper;
import com.example.android.inventory.data.OtakuProvider;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;// Identifier for the pet data loader
    OtakuDbHelper helper; // Database helper
    Cursor cursor;
    static OtakuCursorAdapter adapter; // Adapter for listView
    ListView list; // ListView to display products

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Textview of App Title
        TextView header = (TextView) findViewById(R.id.header_title);

        // TextView of text under Quantity color indicators
        TextView lessThan100 = (TextView) findViewById(R.id.lessThan100);
        TextView lessThan200 = (TextView) findViewById(R.id.lessThan200);
        TextView lessThan300 = (TextView) findViewById(R.id.lessThan300);
        TextView lessThan400 = (TextView) findViewById(R.id.lessThan400);
        TextView moreThan400 = (TextView) findViewById(R.id.moreThan400);

        // Textviews of text that displays when inventory is empty
        TextView emptyTitleText = (TextView) findViewById(R.id.empty_title_text);
        TextView emptySubText = (TextView) findViewById(R.id.empty_subtitle_text);

        // Listview
        list = (ListView) findViewById(R.id.list);

        // Declare typeface for App Title
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Bernardo Moda Bold.ttf");

        // Font for every other textview in this activity
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/feeec.ttf");

        // Set fonts fot each textview
        header.setTypeface(face);
        lessThan100.setTypeface(custom_font);
        lessThan200.setTypeface(custom_font);
        lessThan300.setTypeface(custom_font);
        lessThan400.setTypeface(custom_font);
        moreThan400.setTypeface(custom_font);
        emptyTitleText.setTypeface(custom_font);
        emptySubText.setTypeface(custom_font);

        // Color indicator Key for Low Quant levels ---------------
        ImageView red = (ImageView) findViewById(R.id.red_indicator);
        red.setColorFilter(getResources().getColor(R.color.red_indicator));
        ImageView orange = (ImageView) findViewById(R.id.orange_indicator);
        orange.setColorFilter(getResources().getColor(R.color.orange));
        ImageView yellow = (ImageView) findViewById(R.id.yellow_indicator);
        yellow.setColorFilter(getResources().getColor(R.color.yellow));
        ImageView greenish = (ImageView) findViewById(R.id.greenish_indicator);
        greenish.setColorFilter(getResources().getColor(R.color.greenish));
        ImageView green = (ImageView) findViewById(R.id.green_indicator);
        green.setColorFilter(getResources().getColor(R.color.green));
        //--------------------------------------------------------

        // Floating action button to open the Editor Activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(MainActivity.this, view);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete_all_entries:
                                deleteItems(); // Method to delete every record in Otaku Shop table
                                return true;
                            case R.id.action_add_item: // Intent to open Editor Activity
                                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_dummy_data:
                                insertItem(); // Method to insert 8 mock items into database
                                return true;
                        }
                        return true;
                    }
                });
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.menu_main, menu.getMenu());
                menu.show();

            }
        });

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        list.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        adapter = new OtakuCursorAdapter(this, null);
        list.setAdapter(adapter);

        // onClickLister for each item in the listview.
        // When clicked it opens up the EditorActivity with details about to product
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link OtakuEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventory/inventory/2"
                // if the item with ID 2 was clicked on.
                Uri currentItem = ContentUris.withAppendedId(OtakuContract.OtakuEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItem);

                // Launch the {@link EditActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    /************************************************************************************************/


    public static void saleItem(int id, int quantity) {
        // Call of method in OtakuProvider, which extends content provider passes id and quantity of product
        OtakuProvider.sellItem(id, quantity);

        // Update {@link adapter} with updated quantity information
        adapter.swapCursor(OtakuProvider.updateQuantity());
    }/************************************************************************************************/

    /**
     * Method to delete every record in the table
     */
    private void deleteItems() {
        int uri = getContentResolver().delete(OtakuEntry.CONTENT_URI, null, null);
    }/************************************************************************************************/

    /**
     * Insert Mock Product Items
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys and Otaku Store attributes are the values.
        ContentValues val = new ContentValues();

        /**
         * First Mock Product.
         * First convert drawable into bitmap. Then pass bitmap to getMockProductByte() method to convert bitmap to byte[]
         * This must be done because product Image column is type BLOB so it can only be stored in database as a byte array.
         */
        Bitmap bitmapItem1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item1);
        byte[] img1 = getMockProductByte(bitmapItem1);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img1);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item1Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item1Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item1Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item1Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item1SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item1SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item1SupplierEmail));

        //Insert a new row into the provider using the ContentResolver.
        //Use the {@link OtakuEntry#CONTENT_URI} to indicate that we want to insert
        //into the Otaku Shop database table
        //Recieve the new content URI that will allow us to access this row of data in the futuure
        Uri newUri1 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Second Mock Product
         */
        Bitmap bitmapItem2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item2);
        byte[] img2 = getMockProductByte(bitmapItem2);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img2);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item2Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item2Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item2Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item2Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item2SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item2SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item2SupplierEmail));
        Uri newUri2 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Third Mock Product
         */
        Bitmap bitmapItem3 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item3);
        byte[] img3 = getMockProductByte(bitmapItem3);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img3);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item3Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item3Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item3Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item3Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item3SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item3SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item3SupplierEmail));
        Uri newUri3 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Forth Mock Product
         */
        Bitmap bitmapItem4 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item4);
        byte[] img4 = getMockProductByte(bitmapItem4);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img4);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item4Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item4Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item4Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item4Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item4SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item4SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item4SupplierEmail));
        Uri newUri4 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Fifth Mock Product
         */
        Bitmap bitmapItem5 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item5);
        byte[] img5 = getMockProductByte(bitmapItem5);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img5);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item5Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item5Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item5Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item5Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item5SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item5SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item5SupplierEmail));
        Uri newUri5 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Sixth Mock Product
         */
        Bitmap bitmapItem6 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item6);
        byte[] img6 = getMockProductByte(bitmapItem6);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img6);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item6Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item6Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item6Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item6Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item6SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item6SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item6SupplierEmail));
        Uri newUri6 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Seventh Mock Product
         */
        Bitmap bitmapItem7 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item7);
        byte[] img7 = getMockProductByte(bitmapItem7);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img7);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item7Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item7Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item7Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item7Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item7SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item7SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item7SupplierEmail));
        Uri newUri7 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);

        /**
         * Eigth Mock Product
         */
        Bitmap bitmapItem8 = BitmapFactory.decodeResource(this.getResources(), R.drawable.item8);
        byte[] img8 = getMockProductByte(bitmapItem8);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, img8);
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, getString(R.string.item8Name));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, getString(R.string.item8Category));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, getString(R.string.item8Price));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.item8Quantity));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.item8SupplierName));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, getString(R.string.item8SupplierPhone));
        val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, getString(R.string.item8SupplierEmail));
        Uri newUri8 = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);
    }

    /************************************************************************************************/

    public static byte[] getMockProductByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] img1 = stream.toByteArray();
        return stream.toByteArray();
    }

    /************************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /************************************************************************************************/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                OtakuEntry._ID,
                OtakuEntry.COLUMN_PRODUCT_IMAGE,
                OtakuEntry.COLUMN_PRODUCT_NAME,
                OtakuEntry.COLUMN_PRODUCT_CATEGORY,
                OtakuEntry.COLUMN_PRODUCT_PRICE,
                OtakuEntry.COLUMN_PRODUCT_QUANTITY
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                OtakuEntry.CONTENT_URI, // Provider content URI to query
                projection,  // Columns to include in the resulting Cursor
                null, // No selection clause
                null, // No selection arguments
                null); // Default sort order
    }

    /************************************************************************************************/

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link OtakuCursorAdapter} with this new cursor containing updated pet data
        adapter.swapCursor(data);
    }

    /************************************************************************************************/

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        adapter.swapCursor(null);
    }/************************************************************************************************/


}//End class
