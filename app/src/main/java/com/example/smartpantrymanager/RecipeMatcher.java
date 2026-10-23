package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    private static final double EPSILON = 1e-9;

    // Lowercase, trim, and reduce the last word to its singular form
    // so that "Tomatoes" and "tomato" are treated as the same ingredient.
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            return name;
        }
        String[] words = name.split(" ");
        words[words.length - 1] = singular(words[words.length - 1]);
        return String.join(" ", words);
    }

    private static String singular(String word) {
        String result = word;
        if (result.length() > 4 && result.endsWith("ies")) {
            result = result.substring(0, result.length() - 3) + "y";      // berries -> berry
        } else if (result.length() > 4 && result.endsWith("oes")) {
            result = result.substring(0, result.length() - 2);            // tomatoes -> tomato
        } else if (result.length() > 3 && result.endsWith("s") && !result.endsWith("ss")) {
            result = result.substring(0, result.length() - 1);            // eggs -> egg
        }
        if (result.length() > 3 && result.endsWith("ie")) {
            result = result.substring(0, result.length() - 2) + "y";      // cookie -> cooky
        }
        return result;
    }

    // Units are grouped so that kg/g and l/ml/cup/tbsp/tsp can be compared.
    private static String unitGroup(String unit) {
        switch (unit) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "cup":
            case "tbsp":
            case "tsp":
                return "volume";
            default:
                return "count";
        }
    }

    // Converts a quantity to the base unit of its group (g, ml, or pieces).
    private static double toBase(double quantity, String unit) {
        switch (unit) {
            case "kg":
            case "l":
                return quantity * 1000;
            case "cup":
                return quantity * 240;
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            default:
                return quantity;
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + unitGroup(unit);
    }

    // Adds up the pantry per ingredient and unit group (two "egg" rows are summed).
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String k = key(item.getName(), item.getUnit());
            double amount = toBase(item.getQuantity(), item.getUnit());
            Double current = totals.get(k);
            totals.put(k, (current == null ? 0.0 : current) + amount);
        }
        return totals;
    }

    // STRICT RULE: every ingredient must be present in at least the required quantity.
    private static boolean canMake(Recipe recipe, Map<String, Double> totals) {
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            double required = toBase(ingredient.getQuantity(), ingredient.getUnit());
            Double have = totals.get(key(ingredient.getName(), ingredient.getUnit()));
            if (have == null || have + EPSILON < required) {
                return false;
            }
        }
        return true;
    }

    public static List<Recipe> getSuggestedRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, totals)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }
}