package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");
        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecipeTables(db);
            seedRecipes(db);
        }
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recipes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
    }

    // ---------- Pantry CRUD ----------

    public long addItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("pantry", null, toValues(item));
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, name, quantity, unit, expiry_date FROM pantry ORDER BY name", null)) {
            while (c.moveToNext()) {
                items.add(new PantryItem(
                        c.getInt(0), c.getString(1), c.getDouble(2),
                        c.getString(3), c.getString(4)));
            }
        }
        return items;
    }

    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("pantry", toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName().trim().toLowerCase());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());
        return values;
    }

    // ---------- Recipes (read) ----------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, name, steps FROM recipes ORDER BY name", null)) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                recipes.add(new Recipe(id, c.getString(1), c.getString(2),
                        getIngredientsForRecipe(db, id)));
            }
        }
        return recipes;
    }

    public Recipe getRecipeById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, name, steps FROM recipes WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                return new Recipe(c.getInt(0), c.getString(1), c.getString(2),
                        getIngredientsForRecipe(db, id));
            }
        }
        return null;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT name, quantity, unit FROM recipe_ingredients WHERE recipe_id = ?",
                new String[]{String.valueOf(recipeId)})) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(c.getString(0), c.getDouble(1), c.getString(2)));
            }
        }
        return list;
    }

    // ---------- Recipe seed data ----------

    private static Object[] ing(String name, double quantity, String unit) {
        return new Object[]{name, quantity, unit};
    }

    private void insertRecipe(SQLiteDatabase db, String name, String steps, Object[]... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert("recipes", null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", (String) ingredient[0]);
            iv.put("quantity", (Double) ingredient[1]);
            iv.put("unit", (String) ingredient[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                "1. Beat the eggs with the salt.\n2. Melt the butter in a pan over low heat.\n3. Pour in the eggs and stir gently until just set.",
                ing("egg", 3, "pcs"), ing("butter", 10, "g"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Tomato Omelette",
                "1. Dice the tomato.\n2. Beat the eggs with the salt.\n3. Cook the tomato for 2 minutes, add the eggs and fold when set.",
                ing("egg", 2, "pcs"), ing("tomato", 1, "pcs"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter in a pan and pour in the eggs.\n3. Add the cheese, fold and serve.",
                ing("egg", 3, "pcs"), ing("cheese", 40, "g"), ing("butter", 10, "g"));

        insertRecipe(db, "Pancakes",
                "1. Mix the flour, sugar, milk and egg into a smooth batter.\n2. Pour small rounds into a hot greased pan.\n3. Flip when bubbles appear and cook until golden.",
                ing("flour", 200, "g"), ing("milk", 300, "ml"), ing("egg", 1, "pcs"), ing("sugar", 2, "tbsp"));

        insertRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Whisk in the eggs and flour.\n3. Fry spoonfuls in a pan until golden on both sides.",
                ing("banana", 2, "pcs"), ing("egg", 2, "pcs"), ing("flour", 50, "g"));

        insertRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Dip each slice of bread in the mixture.\n3. Fry both sides until golden.",
                ing("bread", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"), ing("sugar", 1, "tbsp"));

        insertRecipe(db, "Cheese Sandwich",
                "1. Butter the bread.\n2. Place the cheese between two slices.\n3. Toast in a pan until the cheese melts.",
                ing("bread", 2, "pcs"), ing("cheese", 50, "g"), ing("butter", 10, "g"));

        insertRecipe(db, "Garlic Butter Pasta",
                "1. Boil the pasta in salted water.\n2. Melt the butter and fry the crushed garlic.\n3. Toss the drained pasta in the garlic butter.",
                ing("pasta", 200, "g"), ing("garlic", 2, "pcs"), ing("butter", 30, "g"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Spaghetti Aglio e Olio",
                "1. Boil the pasta in salted water.\n2. Warm the olive oil and gently fry the sliced garlic.\n3. Toss the pasta in the oil.",
                ing("pasta", 200, "g"), ing("garlic", 3, "pcs"), ing("olive oil", 3, "tbsp"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Tomato Pasta",
                "1. Boil the pasta.\n2. Fry the garlic in olive oil, add the chopped tomatoes and simmer for 10 minutes.\n3. Mix with the pasta and season.",
                ing("pasta", 200, "g"), ing("tomato", 3, "pcs"), ing("garlic", 1, "pcs"),
                ing("olive oil", 2, "tbsp"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Fried Rice",
                "1. Fry the chopped onion.\n2. Push aside, scramble the eggs in the pan.\n3. Add the cooked rice and soy sauce and stir-fry.",
                ing("rice", 200, "g"), ing("egg", 2, "pcs"), ing("onion", 1, "pcs"), ing("soy sauce", 2, "tbsp"));

        insertRecipe(db, "Rice and Beans",
                "1. Cook the rice.\n2. Fry the onion, add the beans and the salt.\n3. Serve the beans over the rice.",
                ing("rice", 200, "g"), ing("bean", 150, "g"), ing("onion", 1, "pcs"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Chicken and Rice",
                "1. Brown the chicken pieces with the onion.\n2. Add the rice and enough water, season with salt.\n3. Cover and cook until the rice is tender.",
                ing("chicken", 300, "g"), ing("rice", 200, "g"), ing("onion", 1, "pcs"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Mashed Potatoes",
                "1. Boil the peeled potatoes until soft.\n2. Drain and mash with the butter and warm milk.\n3. Season with salt.",
                ing("potato", 4, "pcs"), ing("butter", 30, "g"), ing("milk", 100, "ml"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Roast Potatoes",
                "1. Heat the oven to 200 C.\n2. Toss the cut potatoes with the olive oil and salt.\n3. Roast for 40 minutes, turning once.",
                ing("potato", 4, "pcs"), ing("olive oil", 2, "tbsp"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n2. Simmer them in water for 25 minutes.\n3. Season with salt.",
                ing("carrot", 2, "pcs"), ing("potato", 2, "pcs"), ing("onion", 1, "pcs"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Lentil Soup",
                "1. Fry the onion, carrot and garlic.\n2. Add the lentils and water.\n3. Simmer for 30 minutes and season with salt.",
                ing("lentil", 200, "g"), ing("onion", 1, "pcs"), ing("carrot", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("salt", 1, "tsp"));

        insertRecipe(db, "Tuna Salad",
                "1. Drain the tuna.\n2. Dice the tomato and onion.\n3. Mix everything with the olive oil.",
                ing("tuna", 150, "g"), ing("tomato", 1, "pcs"), ing("onion", 1, "pcs"), ing("olive oil", 1, "tbsp"));

        insertRecipe(db, "Banana Oatmeal",
                "1. Heat the milk and stir in the oats.\n2. Cook for 5 minutes until creamy.\n3. Top with sliced banana.",
                ing("oat", 80, "g"), ing("milk", 250, "ml"), ing("banana", 1, "pcs"));

        insertRecipe(db, "Beef Stir Fry",
                "1. Slice the beef, onion and carrot.\n2. Fry the beef on high heat, then add the vegetables.\n3. Add the soy sauce and cook for 3 minutes.",
                ing("beef", 250, "g"), ing("onion", 1, "pcs"), ing("carrot", 1, "pcs"), ing("soy sauce", 2, "tbsp"));
    }
}