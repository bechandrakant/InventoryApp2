package ml.chandrakant.inventoryapp2;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ml.chandrakant.inventoryapp2.data.ProductContract.ProductEntry;

public class ProductDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    
    private static final int PRODUCT_LOADER = 0;
    private EditText productName;
    private EditText productPrice;
    private EditText productQuantity;
    private EditText supplierName;
    private EditText supplierPhone;

    private Button insertBtn;

    private Uri mCurrentProductUri;

    int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        Button increment = findViewById(R.id.increment_quantity);
        Button decrement = findViewById(R.id.decrement_quantity);

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_product));
            increment.setVisibility(View.GONE);
            decrement.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.edit_product));
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        productName = findViewById(R.id.product_name_et);
        productPrice = findViewById(R.id.product_price_et);
        productQuantity = findViewById(R.id.product_quantity_et);
        supplierName = findViewById(R.id.supplier_name_et);
        supplierPhone = findViewById(R.id.supplier_phone_et);

        insertBtn = findViewById(R.id.insert_btn);
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validData()) {
                    saveProduct();
                    finish();
                    productName.setText("");
                    productPrice.setText("");
                    productQuantity.setText("");
                    supplierName.setText("");
                    supplierPhone.setText("");
                } else {
                    Toast.makeText(ProductDetailsActivity.this,
                            getResources().getString(R.string.enter_valid_data), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button callSupplier = findViewById(R.id.call_supplier_btn);
        callSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialSupplier();
            }
        });


        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementQuantity();
            }
        });
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementQuantity();
            }
        });
    }

    private void dialSupplier() {
        Intent callIntent = new Intent(Intent.ACTION_CALL); //use ACTION_CALL class
        callIntent.setData(Uri.parse("tel:" + supplierPhone.toString()));    //this is the phone number calling

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission from user if the app hasn't got the required permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    10);
            return;
        } else {     // permission granted
            try {
                startActivity(callIntent);  //call activity and make phone call
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_activity), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validData() {
        String pname, pprice, pquantity, suppliername, supplierphone;
        pname = productName.getText().toString().trim();
        pprice = productPrice.getText().toString();
        pquantity = productQuantity.getText().toString();
        suppliername = supplierName.getText().toString().trim();
        supplierphone = supplierPhone.getText().toString();
        boolean isValidate = true;
        if (pname.equals("") || TextUtils.isEmpty(pname)) {
            productName.setError(getResources().getString(R.string.error));
            isValidate = false;
        }
        if (pprice.equals("") || TextUtils.isEmpty(pprice)) {
            productPrice.setError(getResources().getString(R.string.error));
            isValidate = false;
        } else if (Double.valueOf(productPrice.getText().toString().trim()) < 0) {
            productPrice.setError(getResources().getString(R.string.negative_price));
            isValidate = false;
        }
        if (pquantity.equals("") || TextUtils.isEmpty(pquantity)) {
            productQuantity.setError(getResources().getString(R.string.error));
            isValidate = false;
        } else if (Integer.valueOf(productQuantity.getText().toString().trim()) < 0) {
            productQuantity.setError(getResources().getString(R.string.negative_quantity));
            productQuantity.setText("0");
            isValidate = false;
        }
        if (suppliername.equals("") || TextUtils.isEmpty(suppliername)) {
            supplierName.setError(getResources().getString(R.string.error));
            isValidate = false;
        }
        if (supplierphone.equals("") | TextUtils.isEmpty(supplierphone)) {
            supplierPhone.setError(getResources().getString(R.string.error));
            isValidate = false;
        }
        return isValidate;

    }

    private void saveProduct() {
        String nameString = productName.getText().toString().trim();
        String priceString = productPrice.getText().toString().trim();
        String quantityString = productQuantity.getText().toString().trim();
        String supplierString = supplierName.getText().toString().trim();
        String supplierPhoneString = supplierPhone.getText().toString().trim();
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(supplierPhoneString)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_success),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                if (mCurrentProductUri != null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProductDetailsActivity.this);
                    alert.setTitle("Delete Product!?");
                    alert.setIcon(R.drawable.ic_add);
                    alert.setMessage("are you sure that you want to delete this Product ?");
                    alert.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

                            if (rowsDeleted == 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.delete_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.delete_success),
                                        Toast.LENGTH_SHORT).show();
                            }

                            finish();

                        }
                    });
                    alert.setNeutralButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    alert.show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE
        };
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);
            int quantityIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

            String name = cursor.getString(nameIndex);
            int price = cursor.getInt(priceIndex);
            String supplier = cursor.getString(supplierIndex);
            int quantity = cursor.getInt(quantityIndex);
            String supplierPhoneUpdate = cursor.getString(supplierPhoneIndex);

            productName.setText(name);
            productPrice.setText(String.valueOf(price));
            productQuantity.setText(String.valueOf(quantity));
            supplierName.setText(supplier);
            supplierPhone.setText(supplierPhoneUpdate);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        productPrice.setText("");
        productQuantity.setText("");
        supplierName.setText("");
        supplierPhone.setText("");
    }

    public void incrementQuantity() {
        quantity = Integer.valueOf(productQuantity.getText().toString().trim());
        quantity = quantity + 1;
        productQuantity.setText(String.valueOf(quantity));
    }

    public void decrementQuantity() {
        quantity = Integer.valueOf(productQuantity.getText().toString().trim());
        if (quantity > 0) {
            quantity = quantity - 1;
            productQuantity.setText(String.valueOf(quantity));
        }
    }
}
