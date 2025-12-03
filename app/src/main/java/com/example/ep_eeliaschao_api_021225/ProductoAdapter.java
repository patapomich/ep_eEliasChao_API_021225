package com.example.ep_eeliaschao_api_021225;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> productos;

    public ProductoAdapter(ArrayList<Producto> productos) {
        this.productos = productos;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_list_item, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.textViewProductTitle.setText(producto.getTitle());
        holder.textViewProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrice()));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void addProducto(Producto producto) {
        productos.add(0, producto); // Add to the top of the list
        notifyItemInserted(0);
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductTitle;
        TextView textViewProductPrice;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductTitle = itemView.findViewById(R.id.textViewProductTitle);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
        }
    }
}
