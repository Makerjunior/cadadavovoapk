package com.example.casadavovo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private ProgressBar progressBar;
    private EditText searchInput;
    private ImageButton searchButton;

    private static final String API_URL = "http://10.0.2.2:5000/todos"; // Ajuste para emulador Android

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> fetchRecipes(searchInput.getText().toString()));

        fetchRecipes("");
    }

    private void fetchRecipes(String query) {
        new FetchRecipesTask().execute(API_URL + "?s=" + query);
    }

    private class FetchRecipesTask extends AsyncTask<String, Void, List<Recipe>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Recipe> doInBackground(String... params) {
            List<Recipe> recipes = new ArrayList<>();
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    recipes.add(new Recipe(
                            jsonObject.getString("id"),
                            jsonObject.getString("nome"),
                            jsonObject.getString("categoria"),
                            jsonObject.getString("origem"),
                            "http://10.0.2.2:5000/static/images/" + jsonObject.getString("imagem_path"),
                            jsonObject.getString("ingredientes"),
                            jsonObject.getString("instrucoes")
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return recipes;
        }

        @Override
        protected void onPostExecute(List<Recipe> recipes) {
            super.onPostExecute(recipes);
            progressBar.setVisibility(View.GONE);
            recipeList.clear();
            recipeList.addAll(recipes);
            adapter.notifyDataSetChanged();
        }
    }

    // Classe Recipe dentro do mesmo arquivo
    public static class Recipe implements Serializable {
        private String id, nome, categoria, origem, imagemPath, ingredientes, instrucoes;

        public Recipe(String id, String nome, String categoria, String origem, String imagemPath, String ingredientes, String instrucoes) {
            this.id = id;
            this.nome = nome;
            this.categoria = categoria;
            this.origem = origem;
            this.imagemPath = imagemPath;
            this.ingredientes = ingredientes;
            this.instrucoes = instrucoes;
        }

        public String getId() { return id; }
        public String getNome() { return nome; }
        public String getCategoria() { return categoria; }
        public String getOrigem() { return origem; }
        public String getImagemPath() { return imagemPath; }
        public String getIngredientes() { return ingredientes; }
        public String getInstrucoes() { return instrucoes; }
    }

    // Adapter dentro do mesmo arquivo
    private class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
        private List<Recipe> recipes;

        public RecipeAdapter(List<Recipe> recipes) {
            this.recipes = recipes;
        }

        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            Recipe recipe = recipes.get(position);
            holder.name.setText(recipe.getNome());
            holder.category.setText(recipe.getCategoria());
            Glide.with(holder.imageView.getContext()).load(recipe.getImagemPath()).into(holder.imageView);
/*
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(recipe.getNome())
                    .setMessage("Categoria: " + recipe.getCategoria() +
                            "\nOrigem: " + recipe.getOrigem() +
                            "\n\nIngredientes:\n" + recipe.getIngredientes() +
                            "\n\nInstruções:\n" + recipe.getInstrucoes())
                    .setPositiveButton("OK", null)
                    .show();
*/
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class RecipeViewHolder extends RecyclerView.ViewHolder {
            TextView name, category;
            ImageView imageView;

            public RecipeViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.recipeName);
                category = itemView.findViewById(R.id.recipeCategory);
                imageView = itemView.findViewById(R.id.recipeImage);
            }
        }
    }
}
