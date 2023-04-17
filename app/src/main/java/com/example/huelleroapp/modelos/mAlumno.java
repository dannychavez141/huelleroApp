/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.huelleroapp.modelos;

import android.content.Context;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.huelleroapp.Principal;
import com.example.huelleroapp.clases.cDocente;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class mAlumno {

    RequestQueue requestQueu;

    public void obtenerAlumnos( Context ctx,String idaula, String idanio,String server) {
        String api = server+"/asistencia/apis/alumnosApi.php?ac=AAula&aula=" + idaula+"&anio="+idanio;
        Toast.makeText(ctx, api, Toast.LENGTH_LONG).show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(api, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                   Toast.makeText(ctx, response.toString(), Toast.LENGTH_LONG).show();
                try {
                     Principal.listaAlumnos = new Vector();


                    for (int i = 0; i < response.length() ; i++) {
                        jsonObject = response.getJSONObject(i);
                        //Toast.makeText(getApplicationContext(), jsonObject.length(), Toast.LENGTH_LONG).show();
                        String idDoc = jsonObject.getString("idDoc");
                        String dniDoc = jsonObject.getString("dniDoc");
                        String alu = jsonObject.getString("doc");
                        String imghuella1 = jsonObject.getString("imghuella1");
                        String imghuella2 = jsonObject.getString("imghuella2");
                        String foto = jsonObject.getString("foto");

                        cDocente dato = new cDocente(idDoc,dniDoc, alu, imghuella1, imghuella2,foto);

                       // Principal.listaAlumnos.add(dato);
                        //  Toast.makeText(getApplicationContext(),datos.get(i).toString(), Toast.LENGTH_LONG).show();
                    }

                    // Toast.makeText(getApplicationContext(),""+datos.size(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "Esta Aula no posee alumnos Registrados", Toast.LENGTH_LONG).show();
            }
        });
        requestQueu = Volley.newRequestQueue(ctx);
        requestQueu.add(jsonArrayRequest);

    }


}
