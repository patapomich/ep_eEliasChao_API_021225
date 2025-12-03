package com.example.ep_eeliaschao_api_021225;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ProductoAdapter.OnItemClickListener {

    private RecyclerView recyclerViewItems;
    private ArrayList<Producto> listaProductos = new ArrayList<>();
    private ProductoAdapter adapter;
    private final String apiURL = "https://fakestoreapi.com/products";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductoAdapter(listaProductos, this);
        recyclerViewItems.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(view -> mostrarDialogoAgregarProducto());

        new Thread(this::cargarProductos).start();
    }

    private void cargarProductos() {
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder respuesta = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                respuesta.append(linea);
            }
            reader.close();
            con.disconnect();

            JSONArray array = new JSONArray(respuesta.toString());
            ArrayList<Producto> productosTemp = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Producto producto = new Producto();
                producto.setId(obj.getInt("id"));
                producto.setTitle(obj.optString("title", "Sin Título"));
                producto.setPrice(obj.getDouble("price"));
                productosTemp.add(producto);
            }

            runOnUiThread(() -> {
                listaProductos.clear();
                listaProductos.addAll(productosTemp);
                adapter.notifyDataSetChanged();
            });

        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    private void mostrarDialogoAgregarProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Nuevo Producto");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(viewInflated);

        final EditText editTextTitle = viewInflated.findViewById(R.id.editTextTitle);
        final EditText editTextPrice = viewInflated.findViewById(R.id.editTextPrice);
        final EditText editTextDescription = viewInflated.findViewById(R.id.editTextDescription);
        final EditText editTextCategory = viewInflated.findViewById(R.id.editTextCategory);
        final EditText editTextImageUrl = viewInflated.findViewById(R.id.editTextImageUrl);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String priceStr = editTextPrice.getText().toString();
            String description = editTextDescription.getText().toString();
            String category = editTextCategory.getText().toString();
            String imageUrl = editTextImageUrl.getText().toString();

            if (title.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "El título y el precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                Producto nuevoProducto = new Producto();
                nuevoProducto.setTitle(title);
                nuevoProducto.setPrice(price);
                nuevoProducto.setDescription(description);
                nuevoProducto.setCategory(category);
                nuevoProducto.setImage(imageUrl);

                new Thread(() -> agregarProducto(nuevoProducto)).start();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void agregarProducto(Producto producto) {
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("title", producto.getTitle());
            jsonParam.put("price", producto.getPrice());
            jsonParam.put("description", producto.getDescription());
            jsonParam.put("image", producto.getImage());
            jsonParam.put("category", producto.getCategory());

            String jsonInputString = jsonParam.toString();

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder respuesta = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                respuesta.append(linea.trim());
            }
            reader.close();

            JSONObject obj = new JSONObject(respuesta.toString());
            producto.setId(obj.getInt("id")); // La API devuelve el nuevo objeto con un ID

            runOnUiThread(() -> {
                adapter.addProducto(producto);
                recyclerViewItems.scrollToPosition(0);
                Toast.makeText(this, "Producto agregado con ID: " + producto.getId(), Toast.LENGTH_SHORT).show();
            });

            con.disconnect();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error al agregar el producto: " + e.getMessage(), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    @Override
    public void onEditClick(Producto producto) {
        mostrarDialogoEditarProducto(producto);
    }

    private void mostrarDialogoEditarProducto(Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Producto");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        builder.setView(viewInflated);

        final EditText editTextTitle = viewInflated.findViewById(R.id.editTextTitle);
        final EditText editTextPrice = viewInflated.findViewById(R.id.editTextPrice);
        final EditText editTextDescription = viewInflated.findViewById(R.id.editTextDescription);
        final EditText editTextCategory = viewInflated.findViewById(R.id.editTextCategory);
        final EditText editTextImageUrl = viewInflated.findViewById(R.id.editTextImageUrl);

        editTextTitle.setText(producto.getTitle());
        editTextPrice.setText(String.valueOf(producto.getPrice()));
        editTextDescription.setText(producto.getDescription());
        editTextCategory.setText(producto.getCategory());
        editTextImageUrl.setText(producto.getImage());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String priceStr = editTextPrice.getText().toString();
            String description = editTextDescription.getText().toString();
            String category = editTextCategory.getText().toString();
            String imageUrl = editTextImageUrl.getText().toString();

            if (title.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "El título y el precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                Producto productoActualizado = new Producto();
                productoActualizado.setId(producto.getId());
                productoActualizado.setTitle(title);
                productoActualizado.setPrice(price);
                productoActualizado.setDescription(description);
                productoActualizado.setCategory(category);
                productoActualizado.setImage(imageUrl);

                new Thread(() -> actualizarProducto(productoActualizado)).start();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void actualizarProducto(Producto producto) {
        try {
            URL url = new URL(apiURL + "/" + producto.getId());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("title", producto.getTitle());
            jsonParam.put("price", producto.getPrice());
            jsonParam.put("description", producto.getDescription());
            jsonParam.put("image", producto.getImage());
            jsonParam.put("category", producto.getCategory());

            String jsonInputString = jsonParam.toString();

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            reader.close(); // No necesitamos leer la respuesta para PUT

            runOnUiThread(() -> {
                int position = -1;
                for (int i = 0; i < listaProductos.size(); i++) {
                    if (listaProductos.get(i).getId() == producto.getId()) {
                        position = i;
                        break;
                    }
                }
                if (position != -1) {
                    adapter.updateProducto(position, producto);
                }
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
            });

            con.disconnect();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error al actualizar el producto: " + e.getMessage(), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        Producto producto = listaProductos.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que quieres eliminar este producto?")
                .setPositiveButton("Sí", (dialog, which) -> new Thread(() -> eliminarProducto(producto.getId(), position)).start())
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarProducto(int productoId, int position) {
        try {
            URL url = new URL(apiURL + "/" + productoId);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                runOnUiThread(() -> {
                    adapter.removeProducto(position);
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Error al eliminar el producto: " + responseCode, Toast.LENGTH_LONG).show());
            }
            con.disconnect();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error al eliminar el producto: " + e.getMessage(), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }
}
