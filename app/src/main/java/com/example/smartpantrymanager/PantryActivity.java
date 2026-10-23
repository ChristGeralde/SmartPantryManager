package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        txtEmpty = findViewById(R.id.txtEmpty);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(PantryActivity.this, AddEditItemActivity.class);
            intent.putExtra(AddEditItemActivity.EXTRA_ID, item.getId());
            intent.putExtra(AddEditItemActivity.EXTRA_NAME, item.getName());
            intent.putExtra(AddEditItemActivity.EXTRA_QUANTITY, item.getQuantity());
            intent.putExtra(AddEditItemActivity.EXTRA_UNIT, item.getUnit());
            intent.putExtra(AddEditItemActivity.EXTRA_EXPIRY, item.getExpiryDate());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        Button btnAddItem = findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(v ->
                startActivity(new Intent(PantryActivity.this, AddEditItemActivity.class)));

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = dbHelper.getAllItems();
        adapter.setItems(items);
        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}