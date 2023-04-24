package com.example.huelleroapp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.huelleroapp.clases.cDocente;
import com.example.huelleroapp.clases.cClase;
import com.example.huelleroapp.clases.config;
import com.example.huelleroapp.modelos.mAlumno;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SecuGen.FDxSDKPro.*;

public class Principal extends Activity
        implements View.OnClickListener, Runnable, SGFingerPresentEvent {

    private static final String TAG = "SecuGen USB";
    private static final int IMAGE_CAPTURE_TIMEOUT_MS = 10000;
    private static final int IMAGE_CAPTURE_QUALITY = 50;
    private Button mButtonCapture;
    private Button btnBuscar;
    static public TextView txtdatosalu;
    private android.widget.TextView mTextViewResult;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //2014-04-11
    private SGAutoOnEventNotifier autoOn;
    private boolean mAutoOnEnabled = true;
    private byte[] imagen1, imagen2, foto;
    private byte[] imagen1Template;
    private byte[] imagen2Template;
    private boolean bSecuGenDeviceOpened;
    private JSGFPLib sgfplib;
    private boolean usbPermissionRequested;
    static public cDocente[] alumnos;
    static public Vector listaAlumnos;
    RequestQueue requestQueu;
    private config servidor;
    private cDocente alumno;
    private MediaPlayer Sonidoentrada, Sonidoerror;

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////


    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.i("appLog", "USB BroadcastReceiver VID : " + device.getVendorId());
                            Log.i("appLog", "USB BroadcastReceiver PID: " + device.getProductId());
                        } else
                            Log.i("appLog", "mUsbReceiver.onReceive() Device is null");
                    } else
                        Log.i("appLog", "mUsbReceiver.onReceive() permission denied for device " + device);
                }
            }
        }
    };

    public Handler fingerDetectedHandler = new Handler() {
        // @Override
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void handleMessage(Message msg) {
            //Handle the message

            if (mAutoOnEnabled) {
                captura1();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("appLog", "Enter onCreate()");
        servidor = new config(this);
        Sonidoentrada = MediaPlayer.create(this, R.raw.ok);
        Sonidoerror = MediaPlayer.create(this, R.raw.error);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);
        mButtonCapture = (Button) findViewById(R.id.buttonCapture);
        mButtonCapture.setOnClickListener(this);
        btnBuscar = (Button) findViewById(R.id.btnbuscar);
        btnBuscar.setOnClickListener(this);
        mTextViewResult = (TextView) findViewById(R.id.textViewResult);
        txtdatosalu = (TextView) findViewById(R.id.txtdatosAlu);
        servidor = new config(getApplicationContext());
        mButtonCapture.setEnabled(false);
        mImageViewFingerprint = (ImageView) findViewById(R.id.imageViewFingerprint);
        //configurarcion huellero
        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES * JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i = 0; i < grayBuffer.length; ++i)
            grayBuffer[i] = Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.unu);
        mImageViewFingerprint.setImageBitmap(logo);
        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2) * (JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2)];
        for (int i = 0; i < sintbuffer.length; ++i)
            sintbuffer[i] = Color.GRAY;
        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES / 2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES / 2);
        mMaxTemplateSize = new int[1];
        //USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        sgfplib = new JSGFPLib((UsbManager) getSystemService(Context.USB_SERVICE));
        bSecuGenDeviceOpened = false;
        usbPermissionRequested = false;
        mAutoOnEnabled = false;
        autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        Log.i("appLog", "Exit onCreate()");


    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPause() {
        Log.d("appLog", "Enter onPause()");
        if (bSecuGenDeviceOpened) {
            autoOn.stop();
            sgfplib.CloseDevice();
            bSecuGenDeviceOpened = false;
        }
        unregisterReceiver(mUsbReceiver);
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        super.onPause();
        Log.d("appLog", "Exit onPause()");
    }


    @Override
    public void onResume() {
        Log.d(TAG, "Enter onResume()");
        super.onResume();
        registerReceiver(mUsbReceiver, filter);
        long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
            else
                dlgAlert.setMessage("Fingerprint device initialization failed!");
            dlgAlert.setTitle("SecuGen Fingerprint SDK");
            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                            return;
                        }
                    }
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        } else {
            UsbDevice usbDevice = sgfplib.GetUsbDevice();
            if (usbDevice == null) {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage("SecuGen fingerprint sensor not found!");
                dlgAlert.setTitle("SecuGen Fingerprint SDK");
                dlgAlert.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                                return;
                            }
                        }
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            } else {
                boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                if (!hasPermission) {
                    if (!usbPermissionRequested) {
                        //debugMessage("Requesting USB Permission\n");
                        usbPermissionRequested = true;
                        sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
                    } else {
                        //wait up to 20 seconds for the system to grant USB permission
                        hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                        //debugMessage("Waiting for USB Permission\n");
                        int i = 0;
                        while ((hasPermission == false) && (i <= 40)) {
                            ++i;
                            hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
                if (hasPermission) {
                    //debugMessage("Opening SecuGen Device\n");
                    error = sgfplib.OpenDevice(0);
                    // debugMessage("OpenDevice() ret: " + error + "\n");
                    if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        bSecuGenDeviceOpened = true;
                        SGDeviceInfoParam deviceInfo = new SGDeviceInfoParam();
                        error = sgfplib.GetDeviceInfo(deviceInfo);

                        mImageWidth = deviceInfo.imageWidth;
                        mImageHeight = deviceInfo.imageHeight;

                        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                        sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
                        // debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");

                        if (mAutoOnEnabled) {
                            autoOn.start();
                        }
                    } else {
                        //debugMessage("Waiting for USB Permission\n");
                    }
                }
                //Thread thread = new Thread(this);
                //thread.start();
            }
        }
        Log.d(TAG, "Exit onResume()");
    }

    @Override
    public void onDestroy() {
        Log.i("appLog", "Enter onDestroy()");
        sgfplib.CloseDevice();
        sgfplib.Close();
        super.onDestroy();
        Log.i("appLog", "Exit onDestroy()");
    }

    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer) {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }

    public void SGFingerPresentCallback() {
        autoOn.stop();
        fingerDetectedHandler.sendMessage(new Message());
    }

    public void DumpFile(String fileName, byte[] buffer) {

    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void captura1() {
        // Toast.makeText(getApplicationContext(), "Por favor poner su dedo en el Huellero..", Toast.LENGTH_SHORT).show();
        mTextViewResult.setText("Por favor poner su dedo en el Huellero..");
        //  try {
        long result;
        imagen1 = new byte[mImageWidth * mImageHeight];

        result = sgfplib.GetImageEx(imagen1, IMAGE_CAPTURE_TIMEOUT_MS, IMAGE_CAPTURE_QUALITY);

        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            DumpFile("capture2016.raw", imagen1);
            mTextViewResult.setText("Huella  capturada\n");
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(imagen1));

        }
        int pos = comparar();
        if (pos > -1) {
            Toast.makeText(getApplicationContext(), "Identificado", Toast.LENGTH_LONG).show();
            alumno = alumnos[pos];
            llenaralumno(alumno);
        } else {
            Toast.makeText(getApplicationContext(), "Alumno no identificado", Toast.LENGTH_LONG).show();
            Sonidoerror.start();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.unu);
                    mImageViewFingerprint.setImageBitmap(logo);
                    //    Toast.makeText(getApplicationContext(), "Por favor poner su dedo en el Huellero..", Toast.LENGTH_LONG).show();
                    txtdatosalu.setText("______________");
                    mTextViewResult.setText("--------------");
                }
            }, 3000);
        }

       /* }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.i("appLog",e.getMessage());
        }*/
        return;
    }

    public void llenaralumno(cDocente alumno) {
        this.marcarAsistencia();
        txtdatosalu.setText("");
        txtdatosalu.append("DNI: " + alumno.getDniDoc() + "\n");
        txtdatosalu.append("Docente: \n" + alumno.getNombres() + "\n");
        foto = Base64.decode(alumno.getFoto(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(foto, 0, foto.length);
        mImageViewFingerprint.setImageBitmap(decodedByte);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.unu);
                mImageViewFingerprint.setImageBitmap(logo);
                // Toast.makeText(getApplicationContext(), "Por favor poner su dedo en el Huellero..", Toast.LENGTH_LONG).show();
                txtdatosalu.setText("______________");
                mTextViewResult.setText("--------------");
            }
        }, 3000);
    }

    public int comparar() {

        imagen1Template = new byte[(int) mMaxTemplateSize[0]];

        long result;
        try {
            boolean[] matched = new boolean[1];
            result = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            result = sgfplib.CreateTemplate(null, imagen1, imagen1Template);

        } catch (Exception e) {
            mTextViewResult.setText(e.getMessage());
        }
        int pos = -1;
        for (int i = 0; i < alumnos.length; i++) {
            try {

                if (alumnos[i].getImghuella1().length() > 10) {
                    boolean[] matched = new boolean[1];
                    imagen2Template = new byte[(int) mMaxTemplateSize[0]];
                    result = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                    imagen2 = Base64.decode(alumnos[i].getImghuella1(), Base64.DEFAULT);
                    result = sgfplib.CreateTemplate(null, imagen2, imagen2Template);
                    result = sgfplib.MatchTemplate(imagen1Template, imagen2Template, SGFDxSecurityLevel.SL_NORMAL, matched);

                    if (matched[0]) {
                        // mTextViewResult.setText("Iguales!!");
                        pos = i;
                        return pos;
                    }
                    matched = new boolean[1];
                    imagen2Template = new byte[(int) mMaxTemplateSize[0]];
                    result = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                    imagen2 = Base64.decode(alumnos[i].getImghuella2(), Base64.DEFAULT);
                    result = sgfplib.CreateTemplate(null, imagen2, imagen2Template);
                    result = sgfplib.MatchTemplate(imagen1Template, imagen2Template, SGFDxSecurityLevel.SL_NORMAL, matched);

                    if (matched[0]) {
                        //mTextViewResult.setText("Iguales!!");
                        pos = i;
                        return pos;
                    }
                }
            } catch (Exception e) {
                mTextViewResult.setText(e.getMessage());
            }
        }
        return pos;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick(View v) {
        Toast.makeText(this, v.toString(), Toast.LENGTH_LONG);

        if (v == mButtonCapture) {
            captura1();
        }

        if (v == btnBuscar) {

            this.obtenerDocentes();

        }


    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    public void obtenerDocentes() {
        String api = servidor.getServidor() + "app2/apis/apiDocente.php?ac=todosApp";
        //Toast.makeText(getApplicationContext(), api, Toast.LENGTH_LONG).show();
        // Log.i("appLog",api);
        mTextViewResult.setText("Por favor espere, descargando datos...");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(api, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                //Toast.makeText(ctx, response.toString(), Toast.LENGTH_LONG).show();
                try {
                    alumnos = new cDocente[response.length()];
                    for (int i = 0; i < response.length(); i++) {
                        jsonObject = response.getJSONObject(i);
                        //Toast.makeText(getApplicationContext(), jsonObject.length(), Toast.LENGTH_LONG).show();
                        String idDoc = jsonObject.getString("idDoc");
                        String dniDoc = jsonObject.getString("dniDoc");
                        String alu = jsonObject.getString("nomDoc") + " " + jsonObject.getString("apepaDoc") + " " + jsonObject.getString("apemaDoc");
                        String foto = jsonObject.getString("foto");
                        String imghuella1 = jsonObject.getString("imghuella1");
                        String imghuella2 = jsonObject.getString("imghuella2");
                        cDocente docente = new cDocente(idDoc, dniDoc, alu, imghuella1, imghuella2, foto);
                        // txtnomAlu.setText(alu);
                        alumnos[i] = docente;
                    }

                    Toast.makeText(getApplicationContext(), "Datos de Docentes Actualizada", Toast.LENGTH_SHORT).show();
                    mTextViewResult.setText("Bienvenido.");
                    mButtonCapture.setEnabled(true);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "No Existen Registros.", Toast.LENGTH_LONG).show();
                    mButtonCapture.setEnabled(false);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "No Existen Registros", Toast.LENGTH_LONG).show();
            }
        });
        requestQueu = Volley.newRequestQueue(getApplicationContext());
        requestQueu.add(jsonArrayRequest);

    }


    public void marcarAsistencia() {
        String url = servidor.getServidor() + "app2/apis/apiAsistencia.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                mTextViewResult.setText(response);
                Log.i("appLog", response);
                if (response.equals("USTED YA MARCO SU ASISTENCIA Y SU SALIDA")) {
                    Sonidoerror.start();
                } else {
                    Sonidoentrada.start();
                }

                // Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                // Log.i("sql",response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ac", "reg");
                parametros.put("idDoc", alumno.getIdDoc());


                return parametros;
            }
        };
        requestQueu = Volley.newRequestQueue(this);
        requestQueu.add(stringRequest);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    public void run() {

        while (true) {

        }
    }


}
