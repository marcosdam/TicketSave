package com.marcosledesma.ticketsave.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcosledesma.ticketsave.R;
import com.marcosledesma.ticketsave.modelos.Ticket;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdapterTicket extends RecyclerView.Adapter<AdapterTicket.TicketVH>{
    private SimpleDateFormat simpleDateFormat;  // para la fecha

    // Variables necesarias
    private List<Ticket> objects;
    private int resource;
    private Context context;

    // Constructor completo
    public AdapterTicket(List<Ticket> objects, int resource, Context context) {
        this.objects = objects;
        this.resource = resource;
        this.context = context;

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    // Métodos
    @NonNull
    @Override
    public TicketVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        View ticketItem = LayoutInflater.from(context).inflate(resource, null);
        // Modificar disposición aquí (Match parent) - Por algún motivo no funciona en el xml
        ticketItem.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // Retornará un nuevo TicketVH pasándole el ítem del Ticket
        return new TicketVH(ticketItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketVH holder, int position) {
        Ticket ticket = objects.get(position);
        // Asignar Valores a los atributos del holder
        holder.txtComercio.setText(ticket.getComercio());
        holder.txtFecha.setText(simpleDateFormat.format(ticket.getFecha()));
        holder.txtImporte.setText(String.valueOf(ticket.getImporte()));

        // CARD
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "CARD "+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    // clase Ticket ViewHolder
    public static class TicketVH extends RecyclerView.ViewHolder{
        // Elementos de la fila
        ImageView imgImagen;
        TextView txtComercio, txtFecha, txtImporte;
        View card;
        // Instanciarlo en su constructor
        public TicketVH(@NonNull View itemView) {
            super(itemView);
            imgImagen = itemView.findViewById(R.id.imgImagen);
            txtComercio = itemView.findViewById(R.id.txtComercio);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtImporte = itemView.findViewById(R.id.txtImporte);
            card = itemView;
        }
    }
}
