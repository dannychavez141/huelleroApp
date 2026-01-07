package com.example.huelleroapp.clases;

import android.content.Context;
import android.content.SharedPreferences;

public class config {
    private String servidor = "https://unu-fiseic.com/Asistencia/";
    Context con;
    SharedPreferences appSettings;
    public config(Context con) {
        this.con = con;
        this.appSettings = con.getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }



}
