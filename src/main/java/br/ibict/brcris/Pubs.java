package br.ibict.brcris;

import java.util.ArrayList;
import java.util.List;

public class Pubs {
    String id;
    List<InstitutionPub> institutions = new ArrayList<>();


    public Pubs(String id) {
        this.id = id;
    }

    public void addInstitution(InstitutionPub institutionPub) {
        this.institutions.add(institutionPub);
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public List<InstitutionPub> getInstitutions() {
        return institutions;
    }


    public void setInstitutions(List<InstitutionPub> institutions) {
        this.institutions = institutions;
    }



}
