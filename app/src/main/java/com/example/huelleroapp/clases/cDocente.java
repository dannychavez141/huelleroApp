/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.huelleroapp.clases;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Blob;
import java.util.Vector;

public class cDocente {

    private String idDoc;
    private String dniDoc;
    private String nombres;
    private String apepa;
    private String apema;
    private String est;
    private String imghuella1;
    private String imghuella2;
    private String foto;

    public cDocente(String idDoc, String dniDoc, String nombres, String apepa, String apema, String est, String imghuella1, String imghuella2, String foto) {
        this.idDoc = idDoc;
        this.dniDoc = dniDoc;
        this.nombres = nombres;
        this.apepa = apepa;
        this.apema = apema;
        this.est = est;
        this.imghuella1 = imghuella1;
        this.imghuella2 = imghuella2;
        this.foto = foto;
    }

    public cDocente(String idDoc, String dniDoc, String nombres, String imghuella1, String imghuella2, String foto) {
        this.idDoc = idDoc;
        this.dniDoc = dniDoc;
        this.nombres = nombres;
        this.imghuella1 = imghuella1;
        this.imghuella2 = imghuella2;
        this.foto = foto;
    }

    public String getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(String idDoc) {
        this.idDoc = idDoc;
    }

    public String getDniDoc() {
        return dniDoc;
    }

    public void setDniDoc(String dniDoc) {
        this.dniDoc = dniDoc;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApepa() {
        return apepa;
    }

    public void setApepa(String apepa) {
        this.apepa = apepa;
    }

    public String getApema() {
        return apema;
    }

    public void setApema(String apema) {
        this.apema = apema;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }

    public String getImghuella1() {
        return imghuella1;
    }

    public void setImghuella1(String imghuella1) {
        this.imghuella1 = imghuella1;
    }

    public String getImghuella2() {
        return imghuella2;
    }

    public void setImghuella2(String imghuella2) {
        this.imghuella2 = imghuella2;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
