package br.ibict.brcris;

import java.util.ArrayList;
import java.util.List;

public class Publication {
    String id;
    List<Institution> institutions = new ArrayList<>();


    public Publication(String id) {
        this.id = id;
    }

    public void addInstitution(Institution institutionPub) {
        this.institutions.add(institutionPub);
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public List<Institution> getInstitutions() {
        return institutions;
    }


    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }



}
