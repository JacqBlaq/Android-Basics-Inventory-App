package com.example.android.inventory.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.inventory.MainActivity;
import com.example.android.inventory.R;
import com.example.android.inventory.data.OtakuContract.OtakuEntry;
/**
 * Created by Jacquelyn Gboyor on 11/28/2017.
 */

public class OtakuCursorAdapter extends CursorAdapter{

    String updatedQuant;
    int result = 0;

    public OtakuCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(),  "fonts/Quesha.ttf");
        ImageView indicator = (ImageView) view.findViewById(R.id.color_indicator);
        //TextView id = (TextView) view.findViewById(R.id.id);
        ImageView productImg = (ImageView) view.findViewById(R.id.product_Image);
        TextView name = (TextView) view.findViewById(R.id.product_Name);
        TextView price = (TextView) view.findViewById(R.id.product_Price);
        TextView category = (TextView) view.findViewById(R.id.product_Category);
        final TextView quantity = (TextView) view.findViewById(R.id.product_Quantity);
        ImageButton saleButton = (ImageButton) view.findViewById(R.id.sale_Button);
        saleButton.setFocusable(false);


        int idColumnIndex = cursor.getColumnIndex(OtakuEntry._ID);
        int imgColumnIndex = cursor.getColumnIndex(OtakuEntry.COLUMN_PRODUCT_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(OtakuEntry.COLUMN_PRODUCT_NAME);
        int categoryIndex = cursor.getColumnIndex(OtakuEntry.COLUMN_PRODUCT_CATEGORY);
        int priceColumnIndex = cursor.getColumnIndex(OtakuEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(OtakuEntry.COLUMN_PRODUCT_QUANTITY);

        final int pId = cursor.getInt(idColumnIndex);
        byte[] currentimg = cursor.getBlob(imgColumnIndex);
        Bitmap bmp = BitmapFactory.decodeByteArray(currentimg, 0, currentimg.length);
        String currentName = cursor.getString(nameColumnIndex);
        String currentCategory = cursor.getString(categoryIndex);
        String currentPrice = cursor.getString(priceColumnIndex);
        final String currentQuantity = cursor.getString(quantityColumnIndex);
        //int currentQuantity = cursor.getInt(quantityColumnIndex);

        int getQuantity = Integer.parseInt(currentQuantity);
        ColorDrawable colorBackground = (ColorDrawable) indicator.getBackground();
        //GradientDrawable color = (GradientDrawable) indicator.getBackground();


        int colorChoice = 0;
        if (getQuantity >= 400){
            colorChoice = R.color.green;
        }else if (getQuantity >= 100 && getQuantity < 200){
            colorChoice = R.color.orange;
        }else if (getQuantity >= 200 && getQuantity < 300){
            colorChoice = R.color.yellow;
        }else if (getQuantity >= 300 && getQuantity < 400){
            colorChoice = R.color.greenish;
        }else if (getQuantity < 100){
            colorChoice = R.color.red_indicator;
        }

        indicator.getDrawable().setColorFilter(context.getResources().getColor(colorChoice), PorterDuff.Mode.MULTIPLY);
        productImg.setImageBitmap(bmp);
        name.setText(currentName);
        name.setTypeface(custom_font);
        category.setText("Category: " + currentCategory);
        category.setTypeface(custom_font);
        price.setText("Price: $" + currentPrice);
        price.setTypeface(custom_font);
        quantity.setText("Quantity: " + currentQuantity);
        quantity.setTypeface(custom_font);

        //Sale Button onCLickListener
        //Will subtract 1 from quantity of product when displayed in listView
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Call on Method from main activity and pass current item id and current item quantity
                 */
                MainActivity.saleItem(pId, Integer.parseInt(currentQuantity));

            }
        });

    }//End bindView
}//End class
