package com.example.huelleroapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

import android.graphics.Color;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.huelleroapp.clases.cDocente;
import com.example.huelleroapp.clases.config;
import com.example.huelleroapp.modelos.mAlumno;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

public class Registro extends Activity
        implements View.OnClickListener, Runnable, SGFingerPresentEvent {

    private static final String TAG = "SecuGen USB";
    private static final int IMAGE_CAPTURE_TIMEOUT_MS = 10000;
    private static final int IMAGE_CAPTURE_QUALITY = 50;

    private Button mButtonCapture;
    private Button btntemp, btnComp, btnBuscar;
    private Button btnReg;
    private EditText txtcodAlu;
    static public TextView txtnomAlu;
    private android.widget.TextView mTextViewResult;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private ImageView mtemp;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private Bitmap grayBitmap;
    private IntentFilter filter; //2014-04-11
    private SGAutoOnEventNotifier autoOn;
    private boolean mLed;
    private boolean mAutoOnEnabled;
    private byte[] imagen1, imagen2;
    private byte[] imagen1Template;
    private byte[] imagen2Template;
    private boolean bSecuGenDeviceOpened;
    private JSGFPLib sgfplib;
    private boolean usbPermissionRequested;
    static public cDocente alumno;
    RequestQueue requestQueu;
    private config servidor;

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////


    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                         //   debugMessage("USB BroadcastReceiver VID : " + device.getVendorId() + "\n");
                          //  debugMessage("USB BroadcastReceiver PID: " + device.getProductId() + "\n");
                        } else
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                    } else
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
                }
            }
        }
    };

    public Handler fingerDetectedHandler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            //Handle the message
            captura1();
            if (mAutoOnEnabled) {
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Enter onCreate()");
        servidor = new config(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);
        txtcodAlu = (EditText) findViewById(R.id.txtCodAula);
        mButtonCapture = (Button) findViewById(R.id.buttonCapture);
        mButtonCapture.setOnClickListener(this);
        btntemp = (Button) findViewById(R.id.btntemp);
        btntemp.setOnClickListener(this);
        btnComp = (Button) findViewById(R.id.btncomp);
        btnComp.setOnClickListener(this);
        btnBuscar = (Button) findViewById(R.id.btnbuscar);
        btnBuscar.setOnClickListener(this);
        btnReg = (Button) findViewById(R.id.btnGuardar);
        btnReg.setOnClickListener(this);
        mTextViewResult = (TextView) findViewById(R.id.textViewResult);
        txtnomAlu = (TextView) findViewById(R.id.txtdatoClase);
        mImageViewFingerprint = (ImageView) findViewById(R.id.imageViewFingerprint);
        mtemp = (ImageView) findViewById(R.id.imgtemp);
        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES * JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i = 0; i < grayBuffer.length; ++i)
            grayBuffer[i] = Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        mtemp.setImageBitmap(grayBitmap);
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
       // debugMessage("Starting Activity\n");
     //   debugMessage("JSGFPLib version: " + sgfplib.GetJSGFPLibVersion() + "\n");
        mLed = false;
        mAutoOnEnabled = false;
        autoOn = new SGAutoOnEventNotifier(sgfplib, this);
        Log.d(TAG, "Exit onCreate()");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPause() {
        Log.d(TAG, "Enter onPause()");
        if (bSecuGenDeviceOpened) {
            autoOn.stop();
            sgfplib.CloseDevice();
            bSecuGenDeviceOpened = false;
        }
        unregisterReceiver(mUsbReceiver);
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        super.onPause();
        Log.d(TAG, "Exit onPause()");
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

                      //  debugMessage("GetDeviceInfo() ret: " + error + "\n");
                        mImageWidth = deviceInfo.imageWidth;
                        mImageHeight = deviceInfo.imageHeight;
                      //  debugMessage("Image width: " + mImageWidth + "\n");
                      //  debugMessage("Image height: " + mImageHeight + "\n");
                     //   debugMessage("Image resolution: " + mImageDPI + "\n");
                      //  debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");

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
        Log.d(TAG, "Enter onDestroy()");
        sgfplib.CloseDevice();

        sgfplib.Close();
        super.onDestroy();
        Log.d(TAG, "Exit onDestroy()");
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
    public void captura1() {
        long result;
        imagen1 = new byte[mImageWidth * mImageHeight];
        result = sgfplib.GetImageEx(imagen1, IMAGE_CAPTURE_TIMEOUT_MS, IMAGE_CAPTURE_QUALITY);
        String NFIQString = "";
        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            DumpFile("capture2016.raw", imagen2);
            mTextViewResult.setText("Huella 1 capturada\n");
            mImageViewFingerprint.setImageBitmap(this.toGrayscale(imagen1));
        }


        return;
    }

    public void captura2() {
        long result;
        imagen2 = new byte[mImageWidth * mImageHeight];
        result = sgfplib.GetImageEx(imagen2, IMAGE_CAPTURE_TIMEOUT_MS, IMAGE_CAPTURE_QUALITY);
        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            DumpFile("capture2016.raw", imagen2);
            mTextViewResult.setText("Huella 2 capturada\n");
            mtemp.setImageBitmap(this.toGrayscale(imagen2));
        }
        return;
    }


    public void comparar() {

        imagen1Template = new byte[(int) mMaxTemplateSize[0]];
        imagen2Template = new byte[(int) mMaxTemplateSize[0]];
        long result;
        try {
            boolean[] matched = new boolean[1];
            result = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            result = sgfplib.CreateTemplate(null, imagen1, imagen1Template);
            result = sgfplib.CreateTemplate(null, imagen2, imagen2Template);
            result = sgfplib.MatchTemplate(imagen1Template, imagen2Template, SGFDxSecurityLevel.SL_NORMAL, matched);

            if (matched[0]) {
                mTextViewResult.setText("Iguales!!");
            } else {
                mTextViewResult.setText("No Iguales!!");
            }
        } catch (Exception e) {
            mTextViewResult.setText(e.getMessage());
        }
        return;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void onClick(View v) {
        Toast.makeText(this, v.toString(), Toast.LENGTH_LONG);

        if (v == mButtonCapture) {
            captura1();
        }
        if (v == btntemp) {
            captura2();
        }

        if (v == btnComp) {
            comparar();
        }
        if (v == btnBuscar) {
            this.obtenerAlumnos(this.getApplicationContext(), txtcodAlu.getText().toString(), servidor.getServidor());
        }
        if (v == btnReg) {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Importante");
            dialogo1.setMessage("¿ Desea actualizar las huellas del Docente?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    guardar(servidor.getServidor());
                }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                }
            });
            dialogo1.show();
        }


    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    public void obtenerAlumnos(Context ctx, String cod, String server) {
        String api = server + "app2/apis/apiDocente.php?ac=bDni&dniDoc=" + cod;
        Log.i("appLog",api);
       // Toast.makeText(ctx, api, Toast.LENGTH_LONG).show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(api, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                //Toast.makeText(ctx, response.toString(), Toast.LENGTH_LONG).show();
                try {


                    for (int i = 0; i < response.length(); i++) {
                        jsonObject = response.getJSONObject(i);
                        //Toast.makeText(getApplicationContext(), jsonObject.length(), Toast.LENGTH_LONG).show();
                        String id = jsonObject.getString("idDoc");
                        String dniDoc = jsonObject.getString("dniDoc");
                        String alu = jsonObject.getString("nomDoc") + " " + jsonObject.getString("apepaDoc") + " " + jsonObject.getString("apemaDoc");
                        String imghuella1 = jsonObject.getString("imghuella1");
                        String imghuella2 = jsonObject.getString("imghuella2");
                        String foto = jsonObject.getString("foto");
                        Registro.alumno = new cDocente(id, dniDoc, alu, imghuella1, imghuella2, foto);
                        txtnomAlu.setText(alu);

                    }
                    Toast.makeText(ctx, "Docente Encontrado", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueu = Volley.newRequestQueue(ctx);
        requestQueu.add(jsonArrayRequest);

    }

    public void guardar(String server) {
        String url = server + "app2/apis/apiDocente.php";
        //Toast.makeText(this, url, Toast.LENGTH_LONG).show();
        try {
            String h1 = Base64.encodeToString(imagen1, Base64.DEFAULT);
            String h2 = Base64.encodeToString(imagen2, Base64.DEFAULT);
            alumno.setImghuella1(h1);
            alumno.setImghuella2(h2);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            mTextViewResult.setText(e.getMessage());
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               // Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                Log.i("appLog", response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ERROR DE CONEXION", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ac", "recapp");
                parametros.put("0", alumno.getIdDoc());
                parametros.put("1", alumno.getImghuella1());
                parametros.put("2", alumno.getImghuella2());
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
