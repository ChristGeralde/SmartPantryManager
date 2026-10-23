package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "item_id";
    public static final String EXTRA_NAME = "item_name";
    public static final String EXTRA_QUANTITY = "item_quantity";
    public static final String EXTRA_UNIT = "item_unit";
    public static final String EXTRA_EXPIRY = "item_expiry";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "cup", "tbsp", "tsp"};

    private DatabaseHelper dbHelper;
    private EditText edtName, edtQuantity, edtExpiry;
    private Spinner spnUnit;
    private ArrayAdapter<String> unitAdapter;
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        TextView txtFormTitle = findViewById(R.id.txtFormTitle);
        edtName = findViewById(R.id.edtName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtExpiry = findViewById(R.id.edtExpiry);
        spnUnit = findViewById(R.id.spnUnit);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnBack = findViewById(R.id.btnBack);

        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spnUnit.setAdapter(unitAdapter);

        itemId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (itemId != -1) {
            txtFormTitle.setText(R.string.edit_ingredient);
            edtName.setText(getIntent().getStringExtra(EXTRA_NAME));
            edtQuantity.setText(String.valueOf(getIntent().getDoubleExtra(EXTRA_QUANTITY, 0)));
            String expiry = getIntent().getStringExtra(EXTRA_EXPIRY);
            if (expiry != null) {
                edtExpiry.setText(expiry);
            }
            String unit = getIntent().getStringExtra(EXTRA_UNIT);
            if (unit != null) {
                spnUnit.setSelection(unitAdapter.getPosition(unit));
            }
            btnDelete.setVisibility(Button.VISIBLE);
        }

        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveItem() {
        String name = edtName.getText().toString().trim();
        String quantityText = edtQuantity.getText().toString().trim();
        String expiry = edtExpiry.getText().toString().trim();

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            edtName.setError(getString(R.string.error_name_required));
            valid = false;
        }

        double quantity = 0;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            edtQuantity.setError(getString(R.string.error_quantity_invalid));
            valid = false;
        }

        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            edtExpiry.setError(getString(R.string.error_date_invalid));
            valid = false;
        }

        if (!valid) {
            return;
        }

        String unit = spnUnit.getSelectedItem().toString();
        PantryItem item = new PantryItem(
                itemId == -1 ? 0 : itemId, name, quantity, unit,
                expiry.isEmpty() ? null : expiry);

        if (itemId == -1) {
            dbHelper.addItem(item);
        } else {
            dbHelper.updateItem(item);
        }

        Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isValidDate(String text) {
        if (!text.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            format.parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteItem(itemId);
                    Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}