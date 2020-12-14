package com.marcosledesma.ticketsave;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.marcosledesma.ticketsave.adapters.AdapterTicket;
import com.marcosledesma.ticketsave.modelos.Ticket;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // String de la ruta de la imágen para mostrarla
    private String currentPhotoPath;    // Uri al archivo
    ImageView imgImagen;

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

        imgImagen = findViewById(R.id.imgImagen);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Añadir ticket con un Alert Dialog
                // AlertDialog alertDialog = new AlertDialog(this, null);
            }
        });

        imgImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. COMPROBAR QUÉ VERSIÓN DE ANDROID USO (PEDIR PERMISO EXPLÍCITO PARA POSTERIORES A ANDROID 6)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // API 23 (Android 6)
                    abrirCamara();
                } else {
                    // Si tengo los permisos llamo a la función, y si no -> Pedir permisos "manualmente"
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        abrirCamara();
                    } else {  // Lanzamos alerta emergente (Permitir a la App acceder a la cámara?)
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                    }
                }
            }
        });

        imgImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Comprueba versión de Android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    takeSaveAction();
                } else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                        takeSaveAction();
                    } else { // Pide los permisos
                        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, TAKE_SAVE_PERMISSION);

                    }
                }

            }
        });
    }

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
    private void takeSaveAction() {
        try {
            // 1. Crear un Fichero Vacío
            File photoFile = crearFichero();
            // Si photoFile es distinto de null obtendré url interna de la imágen (uri)
            if (photoFile != null) {
                Uri uriPhotoFile = FileProvider.getUriForFile(
                        this,
                        "com.marcosledesma.ejemplo09_permisos",
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

    // ABRIR GALERÍA (botón)
    private void openGalleryAction() {
        Intent intentOpenGallery = new Intent(Intent.ACTION_GET_CONTENT);
        // Filtro para abrir cualquier imagen en cualquier extensión
        intentOpenGallery.setType("image/*");

        startActivityForResult(intentOpenGallery, OPEN_GALLERY_ACTION);
    }
}