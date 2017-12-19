package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.OtakuContract;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class EditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_IMG = 1;
    private static final int EXISTING_ITEM_LOADER = 0;
    private Button contactSupplier;
    private EditText productNameEditText;
    private EditText productPriceEditText;
    private EditText productQantityWditText;
    private EditText productSupplierName;
    private EditText productSupplierPhone;
    private EditText productSupplierEmail;
    private ImageView productImgDisplay;
    private Spinner productCategoriesSpinner;
    private Button minusButton;
    private Button plusButton;
    private Button productImgDisplayButton;
    private String category;
    private Bitmap productImg;
    private byte[] productImgByte;
    private String[] filePath;
    private Uri currentItemUri;
    byte[] productImgByteNew;
    String displayMessage;
    byte[] editedImageView;
    Typeface custom_font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        TextView header = (TextView) findViewById(R.id.add_New_Header);
        contactSupplier = (Button) findViewById(R.id.contact_Supplier);
        productNameEditText = (EditText) findViewById(R.id.product_name);
        productPriceEditText = (EditText) findViewById(R.id.product_price);
        productQantityWditText = (EditText) findViewById(R.id.product_quantity);
        productSupplierName = (EditText) findViewById(R.id.supplier_name);
        productSupplierPhone = (EditText) findViewById(R.id.supplier_phone);
        productSupplierEmail = (EditText) findViewById(R.id.supplier_email);
        productImgDisplay = (ImageView) findViewById(R.id.image_display);
        productCategoriesSpinner = (Spinner) findViewById(R.id.product_category);
        minusButton = (Button) findViewById(R.id.minus_button);
        plusButton = (Button) findViewById(R.id.plus_button);
        productImgDisplayButton = (Button) findViewById(R.id.image_display_button);
        TextView pName = (TextView) findViewById(R.id.pName);
        TextView pPrice = (TextView) findViewById(R.id.pPrice);
        TextView pCategory = (TextView) findViewById(R.id.pCategory);
        TextView pQuantity = (TextView) findViewById(R.id.pQantity);
        TextView pSName = (TextView) findViewById(R.id.pSNAme);
        TextView pSPhone = (TextView) findViewById(R.id.pSPhone);
        TextView pSEmail = (TextView) findViewById(R.id.pSEmail);

        //-----------------------------------------------------------
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're entering a new item or editing an existing one.
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // adding a new product.
        if (currentItemUri == null) {
            // This is a new product, so change the app title to say "Add a New Product"
            header.setText("Add a New Product");

            //Set Visibility of "order more" button to Gone since you're entering a new item
            contactSupplier.setVisibility(View.GONE);
        } else {

            // Otherwise this is an existing item, so change app bar to say "Edit Product"
            header.setText("Edit Product");
            contactSupplier.setVisibility(View.VISIBLE);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }
        //-----------------------------------------------------------

        setSpinner(); //Method to set spinner with String array resource item

        //Font for header
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Bernardo Moda Bold.ttf");
        //Font for Edit Text boxes
        custom_font = Typeface.createFromAsset(getAssets(), "fonts/feeec.ttf");

        //Set font for each textview and editTextBoxes
        header.setTypeface(face);
        pName.setTypeface(custom_font);
        pPrice.setTypeface(custom_font);
        pQuantity.setTypeface(custom_font);
        pCategory.setTypeface(custom_font);
        pSName.setTypeface(custom_font);
        pSPhone.setTypeface(custom_font);
        pSEmail.setTypeface(custom_font);
        productSupplierName.setTypeface(custom_font);
        productSupplierEmail.setTypeface(custom_font);
        productSupplierPhone.setTypeface(custom_font);
        contactSupplier.setTypeface(custom_font);
        productImgDisplayButton.setTypeface(custom_font);


        //-----------------------------------------------------------
        //OnClickLister for Button to decrease quantity by 1
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtractQuant(v);//Calls method
            }
        });//--------------------------------------------------------

        //-----------------------------------------------------------
        //OnClickLister for Button to increase quantity by 1
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuant(v);
            }
        });//--------------------------------------------------------

        //-----------------------------------------------------------
        //onClickListener to set intent for user to select an image from Image gallery
        productImgDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select Picture..."), LOAD_IMG);
            }
        });//--------------------------------------------------------

        //-----------------------------------------------------------
        //FAB to set intent to send user back to main activity
        FloatingActionButton backHome = (FloatingActionButton) findViewById(R.id.back_to_homePage);
        backHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });//--------------------------------------------------------

        //-----------------------------------------------------------
        //When "order more" button is clicked, it opens an Alert dialog box.
        //Which will ask the how they'd like to contact the supplier.
        //Depending on the chose main, an intent will open up an emailing app or telephone.
        contactSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setIcon(R.drawable.communication);
                builder.setMessage("How would you like to contact the supplier?")
                        .setTitle("Contact Supplier")
                        .setPositiveButton("Telephone", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + productSupplierPhone.getText().toString()));
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                                //dialog.cancel();
                            }
                        })
                        .setNegativeButton("Email", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.fromParts("mailto",
                                                productSupplierEmail.getText().toString(),
                                                null));
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Product Order");
                                intent.putExtra(Intent.EXTRA_TEXT, "Hello " + productSupplierName.getText().toString() + ", \n" +
                                        "I would like to order 500 more pcs of " + productNameEditText.getText().toString());
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    //startActivity(intent);
                                    startActivity(Intent.createChooser(intent, "Send email..."));
                                }
                            }
                        });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.show();
            }
        });//--------------------------------------------------------

        //-----------------------------------------------------------
        // FAB to either save or delete the product
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.edit_Options);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(EditActivity.this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_save:
                                insertItem();
                                return true;
                            case R.id.action_delete:

                                //--------------
                                //Alert dialog page that will prompt user to confirm if they want to delete product or not
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                                // 2. Chain together various setter methods to set the dialog characteristics
                                builder.setIcon(R.drawable.delete);
                                builder.setMessage("Are you sure you want to delete this item?")
                                        .setTitle("Delete Product")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteItem();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog dialog = builder.show();
                                //---------------

                                return true;
                            case android.R.id.home:
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                                return true;
                        }
                        return true;
                    }
                });
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_editor, popupMenu.getMenu());
                popupMenu.show();
            }
        });//---------------------------------------------------------
    }//End onCreate

    /************************************************************************************************/
    public void deleteItem() {
        /**Similar to the SQL statement, "DELETE FROM [table name] WHERE COLUMN_PRODUCT_NAME = '[insert product name here]'
         * I am retrieving the current product name from the {@link productNameEditText}
         * Once product is deleted an intent will send user back to MainActivity
         */
        int uri = getContentResolver().delete(
                OtakuContract.OtakuEntry.CONTENT_URI,
                OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME + " LIKE ?",
                new String[]{productNameEditText.getText().toString()});
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }//End deleteItem
    /************************************************************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**Method that is called after user has selected a picture from gallery
         * Try block checks if image was selected or not or if something went wrong
         */
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == LOAD_IMG) {
                    Uri imageSelected = data.getData();
                    filePath = new String[]{MediaStore.Images.Media.DATA};
                    InputStream stream = getContentResolver().openInputStream(imageSelected);
                    productImg = BitmapFactory.decodeStream(stream);
                    productImgDisplay.setImageBitmap(productImg);
                    productImgByte = getBitmap(productImg);
                }
            } else {
                Toast.makeText(this, "You haven't selected an image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }//End onActivityResult
    /************************************************************************************************/


    public static byte[] getBitmap(Bitmap bitmap) {
        /**Turns Bitmap image into a btye array that can be stored in SQLite databse.
         * Images in SQL are saved as BLOB. Byte[] and be converted to a BLOB
         */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }//End getBitmap
    /************************************************************************************************/

    private void subtractQuant(View view) {
        /**Method to subtract 1 item from quantity total of a product
         * Checks if quantity is less than or equal to 0 to prevent a negative quantity
         */
        String quantityVal = productQantityWditText.getText().toString().trim();
        int newVal = Integer.parseInt(quantityVal);
        if (newVal <= 0) {
            productQantityWditText.setText(String.valueOf(0));
        } else {
            productQantityWditText.setText(String.valueOf(newVal - 1));
        }
    }//End subtractQuant
    /************************************************************************************************/

    private void addQuant(View view) {
        //Adds 1 to quantity each time this methid is called
        String quantityVal = productQantityWditText.getText().toString().trim();
        int newVal = Integer.parseInt(quantityVal);
        productQantityWditText.setText(String.valueOf(newVal + 1));
    }//End addQuant
    /************************************************************************************************/


    private void setSpinner() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_of_Categories, android.R.layout.simple_spinner_item);

        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        productCategoriesSpinner.setAdapter(categorySpinnerAdapter);
        productCategoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                category = selection.toString().trim();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }//ENd setSpinner
    /************************************************************************************************/

    private void insertItem() {
        //This method is called when "saved" has been clicked
        String nameString = productNameEditText.getText().toString().trim();
        String priceString = productPriceEditText.getText().toString().trim();
        String categoryString = category;
        String quantityString = productQantityWditText.getText().toString().trim();
        int quant;
        try {
            quant = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            quant = 0;
        }

        String supplierNameString = productSupplierName.getText().toString().trim();
        String supplierPhoneString = productSupplierPhone.getText().toString().trim();
        String supplerEmailString = productSupplierEmail.getText().toString().trim();

        /**
         * Checks if this is a new Item or and Editable Item.
         * If this is a new item then the product image will be set to {@link productImgByte}
         * which was received from the method {@link onActivityResult}
         * If it's a editable item then image byte[] will be set to {@link editedImageView}
         * Which was recieved the {@link insertItem} method.
         * {@link editedImageView} is an instance variable so it can be accessed from every method.
         */
        if (currentItemUri == null) {
            productImgByteNew = productImgByte;
        } else {
            productImgByteNew = editedImageView;
        }

        /**If any of the EditText fields are empty the item will not be saved.
         * {@link displayErrorMess()} Method will be called that will appened text to a string builder.
         * That strng will be displayed through an Alert dialog box, telling the user which EditTextBoxes are mull.
         */
        if ((productImgByteNew == null) || (nameString == null || nameString.length() == 0) || (priceString == null || priceString.length() == 0) ||
                (categoryString == null || categoryString.length() == 0) || (quant < 0) || (supplierNameString == null || supplierNameString.length() == 0) ||
                (supplierPhoneString == null || supplierPhoneString.length() == 0) || (supplerEmailString == null || supplerEmailString.length() == 0)) {

            //assigns results of method to string
            displayMessage = displayErrorMessage(
                    productImgByteNew,
                    nameString,
                    priceString,
                    categoryString,
                    quant,
                    supplierNameString,
                    supplierPhoneString,
                    supplerEmailString);
            AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage(displayMessage)
                    .setIcon(R.drawable.oops)
                    .setTitle("You forgot the following...")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.show();
        } else {
            // If every field contains text then they can be added into the table
            ContentValues val = new ContentValues();
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE, productImgByteNew);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME, nameString);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY, categoryString);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE, priceString);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY, quant);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER, supplierNameString);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE, supplierPhoneString);
            val.put(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL, supplerEmailString);

            // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
            if (currentItemUri == null) {
                // This is a NEW pet, so insert a new pet into the provider,
                // returning the content URI for the new pet.
                Uri newUri = getContentResolver().insert(OtakuContract.OtakuEntry.CONTENT_URI, val);
                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {

                    Toast.makeText(this, "Oh Uh :( Product wasn't saved successfully.", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, "Item Successfully Saved :)", Toast.LENGTH_SHORT).show();

                }
            } else {
                // Otherwise this is an EXISTING Item, so update the product with content URI: currentItemUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because currentItemUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(currentItemUri, val, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, "Oh Uh :( Product wasn't saved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, "Item Successfully saved :)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /************************************************************************************************/

    private String displayErrorMessage(byte[] productImg, String nameString, String priceString, String categoryString, int quant,
                                       String supplierNameString, String supplierPhoneString, String supplerEmailString) {
        //Method that is called to build string that displays which fields are empty
        StringBuilder errorMessages = new StringBuilder();
        if (productImg == null) {
            errorMessages.append("\u25BA Item requires a product picture. \n");
        }
        if (nameString == null || nameString.length() == 0) {
            errorMessages.append("\u25BA Item requires a product name. \n");
        }
        if (priceString == null || priceString.length() == 0) {
            errorMessages.append("\u25BA Item requires a product price. \n");
        }
        if (categoryString == null || categoryString.length() == 0) {
            errorMessages.append("\u25BA Item requires a category \n");
        }
        if (quant < 0) {
            errorMessages.append("\u25BA Item's quantity must be greater than or equal to 0. \n");
        }
        if (supplierNameString == null || supplierNameString.length() == 0) {
            errorMessages.append("\u25BA Item requires a supplier's name.\n");
        }
        if (supplierPhoneString == null || supplierPhoneString.length() == 0) {
            errorMessages.append("\u25BA Item requires a supplier's phone number. \n");
        }
        if (supplerEmailString == null || supplerEmailString.length() == 0) {
            errorMessages.append("\u25BA Item requires a supplier's Email address. \n");
        }

        String finalMessage = errorMessages.toString();
        return finalMessage;
    }//End displayErrorMessage
    /************************************************************************************************/


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
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
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                currentItemUri, // Provider content URI to query
                projection,  // Columns to include in the resulting Cursor
                null, // No selection clause
                null, // No selection arguments
                null); // Default sort order
    }//End onCreateLoader
    /************************************************************************************************/

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int imgColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_NAME);
            int categoryIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_QUANTITY);
            int supNameCOlumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIER);
            int supPhoneColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIERPHONE);
            int supEmailColumnIndex = cursor.getColumnIndex(OtakuContract.OtakuEntry.COLUMN_PRODUCT_SUPPLIEREMAIL);

            //Extract out the value from the Cursor for the given column Index
            byte[] currentimg = cursor.getBlob(imgColumnIndex);
            editedImageView = currentimg;
            Bitmap bmp = BitmapFactory.decodeByteArray(currentimg, 0, currentimg.length);
            String currentName = cursor.getString(nameColumnIndex);
            String currentCategory = cursor.getString(categoryIndex);
            String currentPrice = cursor.getString(priceColumnIndex);
            String currentQuantity = cursor.getString(quantityColumnIndex);
            String currentSupName = cursor.getString(supNameCOlumnIndex);
            String currentSupPhone = cursor.getString(supPhoneColumnIndex);
            String currentSupEmail = cursor.getString(supEmailColumnIndex);

            productNameEditText.setText(currentName);
            productNameEditText.setTypeface(custom_font);
            productPriceEditText.setText(currentPrice);
            productPriceEditText.setTypeface(custom_font);

            productImgDisplay.setImageBitmap(bmp);
            String[] categorySpinner = getResources().getStringArray(R.array.array_of_Categories);
            productCategoriesSpinner.setSelection(Arrays.asList(categorySpinner).indexOf(currentCategory));

            productQantityWditText.setText(currentQuantity);
            productQantityWditText.setTypeface(custom_font);
            productSupplierName.setText(currentSupName);
            productSupplierName.setTypeface(custom_font);
            productSupplierPhone.setText(currentSupPhone);
            productSupplierPhone.setTypeface(custom_font);
            productSupplierEmail.setText(currentSupEmail);
            productSupplierEmail.setTypeface(custom_font);

        }
    }//End onLoadFinished
    /************************************************************************************************/

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        productNameEditText.setText("");
        productPriceEditText.setText("");
        productImgDisplay.setImageBitmap(null);
        productCategoriesSpinner.setSelection(0);
        productQantityWditText.setText("");
        productSupplierName.setText("");
        productSupplierPhone.setText("");
        productSupplierEmail.setText("");
    }//End onLoaderReset
    /************************************************************************************************/
}//End class
