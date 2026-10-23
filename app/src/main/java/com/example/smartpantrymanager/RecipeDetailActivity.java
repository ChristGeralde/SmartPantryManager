package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipeById(getIntent().getIntExtra(EXTRA_RECIPE_ID, -1));
        if (recipe == null) {
            finish();
            return;
        }

        TextView txtName = findViewById(R.id.txtDetailName);
        TextView txtIngredients = findViewById(R.id.txtDetailIngredients);
        TextView txtSteps = findViewById(R.id.txtDetailSteps);

        txtName.setText(recipe.getName());

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredients.append("\u2022 ")
                    .append(ingredient.getName())
                    .append(" - ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append("\n");
        }
        txtIngredients.setText(ingredients.toString().trim());
        txtSteps.setText(recipe.getSteps());
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}