package v.viia.com.viia_movil;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Task extends AppCompatActivity {
    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;


    private Bitmap bitmap;
    private ImageView mSetImage;
    private Button mOptionButton;
    private Button mSubir,mSubirTelerik;
    private RelativeLayout mRlView;
    private String mPath;
    private ProgressDialog mProgress;




    private TextView mTv_user;
    private EditText mEditText_punto, mEditText_observacion,mEditText_fecha;
    //Servicio
    //private String UploadUrl = "http://legalmovil.com/service/updateinf2o.php";
    private String UploadUrl = "http://legalmovil.com/invian/welcome/nombre";
    private String urlTelerikServiceI = "https://api.everlive.com/v1/e82dy3vmux1jchlu/VT_incidencia/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        mEditText_punto = (EditText) findViewById(R.id.editText_punto);
        mEditText_observacion = (EditText) findViewById(R.id.editText_observacion);
        mEditText_fecha = (EditText) findViewById(R.id.editText_fecha);

        mTv_user = (TextView) findViewById(R.id.tv_user);
        mTv_user.setText(""+getIntent().getExtras().getString("rUser"));

        // mEditText_user.setText(usuario);

        mProgress = new ProgressDialog(this);
        mSetImage = (ImageView) findViewById(R.id.set_picture);
        mOptionButton = (Button) findViewById(R.id.show_options_button);
        mSubir = (Button) findViewById(R.id.subir);
        mRlView = (RelativeLayout) findViewById(R.id.activity_task);


        mSubirTelerik =(Button)  findViewById(R.id.btnTelerik);

        if(mayRequestStoragePermission()) {
            mOptionButton.setEnabled(true);
        }else {
            mOptionButton.setEnabled(false);
        }

        mSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upImage();
                mProgress.setMessage("Cargando...");
                mProgress.show();
            }
        });
        mOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();

            }
        });
        mSubirTelerik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTelerik();
                mProgress.setMessage("Enviando a telerik...");
                mProgress.show();
            }
        });
    }
    private boolean mayRequestStoragePermission() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(mRlView, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(Task.this);
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Tomar foto"){
                    openCamera();
                }else if(option[which] == "Elegir de galeria"){
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            Log.v("TAG_IMAGEN","IMAGEN GUARDADA"+newFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);


        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });

                    bitmap = BitmapFactory.decodeFile(mPath);
                    mSetImage.setImageBitmap(bitmap);



                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    mSetImage.setImageURI(path);

                    break;

            }
        }
    }

    private void upImage(final String getFecha) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,UploadUrl ,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                mProgress.dismiss();

                //mSetImage.setImageResource(0);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Task.this,"error",Toast.LENGTH_LONG).show();
                mProgress.dismiss();

            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                String imagen_camara = imageToString(bitmap);
                String userEmail = getIntent().getExtras().getString("rUser");
                String obser= mEditText_observacion.getText().toString().trim();
                String punt= mEditText_punto.getText().toString().trim();


                String name ="Viia Task";
                String usuario =userEmail;
                String punto =punt;
                String observacion =obser;
                String fecha ="";


                params.put("name",name);
                params.put("usuario",usuario);
                params.put("punto",punto);
                params.put("observacion",observacion);
                params.put("fecha",getFecha);

                 params.put("image",imagen_camara);
               // Log.v("imagen_camara",h);
               // Log.v("TAGA",imagen_select);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /*Enviar a telerik*/
    private void upTelerik() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,urlTelerikServiceI ,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(),"Enviado Correctamente a Telerik",Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Task.this,"Hubo un error en el envio a telerik",Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                String userEmail = getIntent().getExtras().getString("rUser");
                String punto_= mEditText_punto.getText().toString().trim();
                String obserservacion_= mEditText_observacion.getText().toString().trim();

                /*Obtener Fecha en dispositivo*/
                SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss");
                String getFecha = s.format(new Date());
                String name ="Viia Task";
                String usuario =userEmail;
                String punto =punto_;
                String observacion =obserservacion_;
                String urlImagen ="http://legalmovil.com/invian/public/uploads/VT_"+getFecha+".jpg";

                params.put("name",name);
                params.put("usuario",usuario);
                params.put("punto",punto);
                params.put("observacion",observacion);
                params.put("urlImagen",urlImagen);
                params.put("fecha",getFecha);
                upImage(getFecha);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Task.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                mOptionButton.setEnabled(true);
            }
        }else{
            showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Task.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,10,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }
}