package com.example.ep_eeliaschao_api_021225;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> productos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Producto producto);
        void onDeleteClick(int position);
    }

    public ProductoAdapter(ArrayList<Producto> productos, OnItemClickListener listener) {
        this.productos = productos;
        this.listener = listener;
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

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(producto));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void addProducto(Producto producto) {
        productos.add(0, producto);
        notifyItemInserted(0);
    }
    public void updateProducto(int position, Producto producto) {
        productos.set(position, producto);
        notifyItemChanged(position);
    }

    public void removeProducto(int position) {
        productos.remove(position);
        notifyItemRemoved(position);
    }


    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductTitle;
        TextView textViewProductPrice;
        Button buttonEdit, buttonDelete;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductTitle = itemView.findViewById(R.id.textViewProductTitle);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
