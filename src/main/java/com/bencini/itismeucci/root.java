package com.bencini.itismeucci;

import java.util.ArrayList;

public class root {
    private String annoDiInizio;
    aula aula;
    String classe;
    String numeroFinestre;
    String specializzazione;
    ArrayList<element> studenti;

    public String getAnnoDiInizio() {
        return annoDiInizio;
    }

    public aula getAula() {
        return aula;
    }

    public String getClasse() {
        return classe;
    }

    public String getNumeroFinestre() {
        return numeroFinestre;
    }

    public String getSpecializzazione() {
        return specializzazione;
    }

    public ArrayList<element> getStudenti() {
        return studenti;
    }
}