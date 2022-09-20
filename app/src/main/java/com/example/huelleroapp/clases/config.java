package com.example.huelleroapp.clases;

import android.content.Context;
import android.content.SharedPreferences;

public class config {
    private String servidor = "http://www.upcyrm.com/";
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
/*
    public void salvarDatos(mUsuario Usu) {
//nos ponemos a editar las preferencias
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putString("dni", Usu.getDni());
        editor.putString("nombres", Usu.getNombres());
// quizás para adelante, chequear por timeout
        editor.putString("apellidos", Usu.getApellidos());
        editor.putString("clave", Usu.getClave());
// Commit la edición
        editor.commit();
    }

    public void borrarDatos() {
//editamos nuevamente las preferencias
        SharedPreferences.Editor editor = appSettings.edit();
// removemos los datos agregados
        editor.remove("dni");
        editor.remove("nombres");
// quizás para adelante, chequear por timeout
        editor.remove("apellidos");
        editor.remove("clave");
// Commit la edición
        editor.commit();
    }

    // finalmente chequeamos si esta logueado o hay que pedirle que se loguee.
// al iniciar la app luego de un splash logo o algo asi: startActivity(crearIntent());
    public mUsuario verificar() {
//le pedimos a la configuración que nos de el id del usuario
// si no lo encuentra, por omisión -1
        mUsuario user = new mUsuario("", "", "", "");
        String dni = appSettings.getString("dni", null);
        String nom = appSettings.getString("nombres", null);
        String ape = appSettings.getString("apellidos", null);
        String tipo = appSettings.getString("clave", null);
        user = new mUsuario(dni, nom, ape, tipo);
        return user;
    }

*/


}
