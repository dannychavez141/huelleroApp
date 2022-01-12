package com.example.huelleroapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class menu extends Activity implements View.OnClickListener {
    private Button asistencia, registrar, salir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        asistencia = findViewById(R.id.btnmarcar);
        asistencia.setOnClickListener(this);
        registrar = findViewById(R.id.btnregistrar);
        registrar.setOnClickListener(this);
        salir = findViewById(R.id.btnsalir);
        salir.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == asistencia) {
            Intent intent = new Intent (v.getContext(), Principal.class);
            startActivityForResult(intent, 0);
        }
        if (v == registrar) {
            Intent intent = new Intent (v.getContext(), Registro.class);
            startActivityForResult(intent, 0);
        }
        if (v == salir) {
            finish();
        }
    }
}