package com.example.huelleroapp.clases;

public class cClase {
    private String id,asig,docente,curso,fecha,hini,hfin,aula,anio,est;

    public String getAsig() {
        return asig;
    }

    public void setAsig(String asig) {
        this.asig = asig;
    }

    public cClase(String id, String asig, String docente, String curso, String fecha, String hini, String hfin, String aula, String anio, String est) {
        this.id = id;
        this.asig = asig;
        this.docente = docente;
        this.curso = curso;
        this.fecha = fecha;
        this.hini = hini;
        this.hfin = hfin;
        this.aula = aula;
        this.anio = anio;
        this.est = est;
    }

    public cClase(String id, String docente, String curso, String fecha, String hini, String hfin, String aula, String anio, String est) {
        this.id = id;
        this.docente = docente;
        this.curso = curso;
        this.fecha = fecha;
        this.hini = hini;
        this.hfin = hfin;
        this.aula = aula;
        this.anio = anio;
        this.est = est;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHini() {
        return hini;
    }

    public void setHini(String hini) {
        this.hini = hini;
    }

    public String getHfin() {
        return hfin;
    }

    public void setHfin(String hfin) {
        this.hfin = hfin;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }
}
