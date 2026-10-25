# Smart Pantry Manager

An Android app (Java) that helps reduce food waste. The user records the ingredients they
have at home, and the app suggests only the recipes that can be cooked **strictly** with
those ingredients: a recipe is shown only if every ingredient is present in at least the
required quantity.

## Features
- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- Input validation on the ingredient form
- 20 pre-loaded recipes stored in the database
- Strict matching that tolerates case, singular/plural names (tomato / tomatoes)
  and simple unit conversion (kg/g, l/ml, cup/tbsp/tsp)
- Suggested recipes screen and recipe detail screen
- Settings screen with an option to highlight ingredients expiring within 3 days

## Database
**SQLite** (SQLiteOpenHelper), stored locally on the device.

Why SQLite: no server or account is needed, the data persists after the app is closed, and
the data model is small (pantry, recipes, recipe_ingredients), so a local database is enough.

## Setup and run
1. Install Android Studio and open this project.
2. Wait for the Gradle sync to finish.
3. Start an emulator (tested on Pixel 6, API 34) or connect a phone with USB debugging enabled.
4. Click Run. The 20 recipes are created automatically on first launch.

## Technologies
Java, Android Studio, RecyclerView with custom adapters, Intents, SharedPreferences, SQLite.

## Author
Christ Geralde Mpaga, student number 402312848