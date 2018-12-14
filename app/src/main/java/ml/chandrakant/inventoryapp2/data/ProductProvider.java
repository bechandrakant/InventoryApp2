package ml.chandrakant.inventoryapp2.data;

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
import android.widget.Toast;

import ml.chandrakant.inventoryapp2.R;

public class ProductProvider extends ContentProvider {
    private static final int PRODUCT = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher mInventoryUriMatcher = buildProductsUriMatcher();

    public static UriMatcher buildProductsUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Directory All Products
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT, PRODUCT);
        // Single Product
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT + "/#", PRODUCT_ID);
        return uriMatcher;
    }

    private ProductDbHelper productDbHelper;

    @Override
    public boolean onCreate() {
        productDbHelper = new ProductDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = productDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = mInventoryUriMatcher.match(uri);
        switch (match) {

            case PRODUCT:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.unable_to_query) + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = mInventoryUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProductMethod(uri, contentValues);
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.insert_failed) + uri);
        }
    }

    private Uri insertProductMethod(Uri uri, ContentValues values) {
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_product_name));
        }
        Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_product_price));
        }
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_product_quantity));
        }
        String supplierName = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_supplier_name));
        }
        String supplierPhone = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_supplier_phone));
        }
        long id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.insert_failed) + uri,
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = productDbHelper.getWritableDatabase();
        int NoRowsDeleted;

        final int match = mInventoryUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                NoRowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                NoRowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.unable_to_delete) + uri);
        }
        if (NoRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return NoRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mInventoryUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProductData(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProductData(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.update_failed) + uri);
        }
    }

    private int updateProductData(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_product_name));
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_supplier_name));
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierPhone = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_supplier_phone));
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = contentValues.getAsDouble(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null && price < 0) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.no_product_price));
            }
        }
        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.negative_quantity), Toast.LENGTH_SHORT).show();
            }
        }
        if (contentValues.size() == 0) {
            return 0;
        }
        int rowsUpdated = db.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
