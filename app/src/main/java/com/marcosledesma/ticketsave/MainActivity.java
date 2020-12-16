package com.marcosledesma.ticketsave;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.marcosledesma.ticketsave.adapters.AdapterTicket;
import com.marcosledesma.ticketsave.modelos.Ticket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    // String de la ruta de la imágen para mostrarla
    private SimpleDateFormat simpleDateFormat;  // para la fecha
    private String currentPhotoPath;    // Uri al archivo

    // Vista
    private ImageView imgImagen;
    private TextView txtComercio, txtFecha, txtImporte;

    // Request para devolver info
    private final int CAMARA_ACTION = 1;
    private final int TAKE_SAVE_ACTION = 2;
    private final int OPEN_GALLERY_ACTION = 3;

    // Request Code de los permisos
    private final int CAMERA_PERMISSION = 100;
    private final int TAKE_SAVE_PERMISSION = 101;
    private final int OPEN_GALLERY_PERMISSION = 102;

    // Modelo de datos
    private ArrayList<Ticket> listaTickets;
    // Fila
    private int filaTicket;
    // RecyclerView
    private RecyclerView recyclerView;
    // ADAPTER
    private AdapterTicket adapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar
        filaTicket = R.layout.ticket_card;
        listaTickets = new ArrayList<>();
        recyclerView = findViewById(R.id.contenedorTickets);

        // Permite tener diferentes Layouts (Contenedores -> "estructurantes")
        LinearLayoutManager linearLayoutManagerVertical = new LinearLayoutManager(this);    // Vertical por defecto
        recyclerView.setLayoutManager(linearLayoutManagerVertical);

        // Iniciar adapter y asignarlo al Recycler
        adapter = new AdapterTicket(listaTickets, filaTicket, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true); // Todas las filas tendrán el mismo tamaño
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // Vista (probando)
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        imgImagen = findViewById(R.id.imgImagen);
        txtComercio = findViewById(R.id.txtComercio);
        txtFecha = findViewById(R.id.txtFecha);
        txtImporte = findViewById(R.id.txtImporte);

        //imgImagen.setImageBitmap(null);
        //txtComercio.setText("Mercadona");
        //txtFecha.setText(simpleDateFormat.format(Date.from(Instant.now())));
        //txtImporte.setText(String.valueOf(3.5f));


        // PARTE 1.
        // Hacer y guardar foto
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PERMISOS -> Abrir cámara && Hacer y Guardar la foto && Abrir Galería
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    abrirCamara();
                    hacerYguardarFoto();
                    abrirGaleria();
                } else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED
                        ){
                        abrirCamara();
                        hacerYguardarFoto();
                        abrirGaleria();
                    } else { // Pide los permisos de cámara y escritura para hacer y guardar la foto
                        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, CAMERA_PERMISSION);
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, TAKE_SAVE_PERMISSION);
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, OPEN_GALLERY_PERMISSION);
                    }
                }
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCIONES
    // Abrir la cámara (se abrirá al pulsar el Floating Action Button)
    private void abrirCamara() {
        // Abrir cámara
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // startActivityForResult porque ésta nos devolverá una imagen
        startActivityForResult(intentCamara, CAMARA_ACTION);
    }

    // CREAR FICHERO VACÍO (DESPUÉS LA CÁMARA LO RELLENARÁ CON LA FOTO TOMADA)
    private File crearFichero() throws IOException {
        // Momento en que presionamos botón (guardado en timeStamp)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Nombre completo de la imágen
        String imageFileName = "JPEG_" + timeStamp + "_";

        File directoryPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, // Nombre de la imágen
                ".jpg", // Extensión
                directoryPictures); // Ruta donde almacenar
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // RECOGE EL FICHERO VACÍO Y GUARDA LA FOTO TOMADA EN LA URI
    private void hacerYguardarFoto() {
        try {
            // 1. Crear un Fichero Vacío
            File photoFile = crearFichero();
            // Si photoFile es distinto de null obtendré url interna de la imágen (uri)
            if (photoFile != null) {
                Uri uriPhotoFile = FileProvider.getUriForFile(
                        this,
                        "com.marcosledesma.ticketsave",
                        photoFile);
                // Intent
                Intent intentTakeSave = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Le pasamos parámetro (enlace) donde queremos que se guarde
                intentTakeSave.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile);
                // Así la cámara tendrá la ruta donde guardar esa imágen
                //
                startActivityForResult(intentTakeSave, TAKE_SAVE_ACTION);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ABRIR GALERÍA
    private void abrirGaleria() {
        Intent intentOpenGallery = new Intent(Intent.ACTION_GET_CONTENT);
        // Filtro para abrir cualquier imagen en cualquier extensión
        intentOpenGallery.setType("image/*");

        startActivityForResult(intentOpenGallery, OPEN_GALLERY_ACTION);
    }


    /**
     * Se ejecuta justo después de que el usuario conteste a los permisos
     *
     * @param requestCode  -> código de la petición de los permisos
     * @param permissions  -> String[] con los permisos que se han solicitado
     * @param grantResults ->  int[] con los resultados de las peticiones de cada permiso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Comprobar si has dado permisos o no (en qué posición de los array de String e int)
        if (requestCode == CAMERA_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "No puedo abrir la cámara sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        // Lo mismo para guardar la foto tomada (Este debe comprobar 2 permisos -> Cámara y escritura)
        if (requestCode == TAKE_SAVE_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                hacerYguardarFoto();
            } else {
                Toast.makeText(this, "No tengo permisos de cámara ni de escritura", Toast.LENGTH_SHORT).show();
            }
        }

        // Lo mismo para Leer Almacenamiento Externo
        if (requestCode == OPEN_GALLERY_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                Toast.makeText(this, "No tengo permisos de lectura", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Para mostrar la foto realizada en el ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // imageBitmap  -> camaraAction
        if (requestCode == CAMARA_ACTION && resultCode == RESULT_OK && data != null) {
            Bundle bundle = new Bundle();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");   // CAST del bundle
            imgImagen.setImageBitmap(imageBitmap);
            txtComercio.setText("Mercadona");
            txtFecha.setText(simpleDateFormat.format(Date.from(Instant.now())));
            txtImporte.setText(String.valueOf(3.5f));
        }

        // takeSaveAction (currentPhotoPath -> Uri con la img)
        if (requestCode == TAKE_SAVE_ACTION && resultCode == RESULT_OK) {
            imgImagen.setImageURI(Uri.parse(currentPhotoPath));
            txtComercio.setText("Mercadona");
            txtFecha.setText(simpleDateFormat.format(Date.from(Instant.now())));
            txtImporte.setText(String.valueOf(3.5f));
        }

        // startActivityForResult para Abrir Galería
        if (requestCode == OPEN_GALLERY_ACTION && resultCode == RESULT_OK && data != null) {
            Uri uriFile = data.getData();
            imgImagen.setImageURI(uriFile);
            txtComercio.setText("Mercadona");
            txtFecha.setText(simpleDateFormat.format(Date.from(Instant.now())));
            txtImporte.setText(String.valueOf(3.5f));
        }
    }
}