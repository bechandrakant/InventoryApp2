package ml.chandrakant.inventoryapp2.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import ml.chandrakant.inventoryapp2.R;

public class ProductCursorAdapter extends CursorAdapter {


    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView mProductName = view.findViewById(R.id.tv_product_name);
        TextView mProductPrice = view.findViewById(R.id.tv_product_price);
        TextView mProductQuantity = view.findViewById(R.id.tv_product_quantity);
        Button mSaleButton = view.findViewById(R.id.btn_sale);

        int productNameIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int productQuantityIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productPriceIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int idIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        String productnameString = cursor.getString(productNameIndex);
        final int quantityInteger = cursor.getInt(productQuantityIndex);
        int price = cursor.getInt(productPriceIndex);
        final long id = cursor.getLong(idIndex);
        mSaleButton.setFocusable(false);
        if (quantityInteger >= 0) {
            mSaleButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   ContentValues values = new ContentValues();
                                                   if (quantityInteger == 0) {
                                                       values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInteger);
                                                   } else {
                                                       values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInteger - 1);
                                                   }
                                                   Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                                                   context.getContentResolver().update(
                                                           uri,
                                                           values,
                                                           ProductContract.ProductEntry._ID + "=?",
                                                           new String[]{String.valueOf(ContentUris.parseId(uri))});
                                               }

                                           }
            );

            mProductName.setText(productnameString);
            mProductQuantity.setText(String.format("Quantity: %s", String.valueOf(quantityInteger)));
            mProductPrice.setText(String.format("Price: $ %s", String.valueOf(price)));

        } else {

            Toast.makeText(context, "Quantity is Zero", Toast.LENGTH_SHORT).show();

        }
    }
}
