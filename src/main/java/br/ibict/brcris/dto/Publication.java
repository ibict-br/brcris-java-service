package br.ibict.brcris.dto;

import java.util.HashSet;
import java.util.Set;

public class Publication {
    String id;
    Set<Institution> institutions = new HashSet<>();


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


    public Set<Institution> getInstitutions() {
        return institutions;
    }


    public void setInstitutions(Set<Institution> institutions) {
        this.institutions = institutions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Publication other = (Publication) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }



}
