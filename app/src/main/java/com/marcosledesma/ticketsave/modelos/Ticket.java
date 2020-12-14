package com.marcosledesma.ticketsave.modelos;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Ticket implements Parcelable {
    private String comercio;
    private Uri foto;
    private Date fecha;
    private float importe;

    public Ticket() {
    }

    public Ticket(String comercio, Uri foto, Date fecha, float importe) {
        this.comercio = comercio;
        this.foto = foto;
        this.fecha = fecha;
        this.importe = importe;
    }

    protected Ticket(Parcel in) {
        comercio = in.readString();
        foto = in.readParcelable(Uri.class.getClassLoader());
        fecha= new Date(in.readLong());     // Para la fecha
        importe = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comercio);
        dest.writeParcelable(foto, flags);
        dest.writeLong(fecha.getTime());    // Para la fecha
        dest.writeFloat(importe);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ticket> CREATOR = new Creator<Ticket>() {
        @Override
        public Ticket createFromParcel(Parcel in) {
            return new Ticket(in);
        }

        @Override
        public Ticket[] newArray(int size) {
            return new Ticket[size];
        }
    };

    public String getComercio() {
        return comercio;
    }

    public void setComercio(String comercio) {
        this.comercio = comercio;
    }

    public Uri getFoto() {
        return foto;
    }

    public void setFoto(Uri foto) {
        this.foto = foto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public float getImporte() {
        return importe;
    }

    public void setImporte(float importe) {
        this.importe = importe;
    }
}
