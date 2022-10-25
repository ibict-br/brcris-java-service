package br.ibict.brcris;

import java.util.HashSet;
import java.util.Set;

public class Author {

    String id;
    String name;
    Set<String> pubs = new HashSet<>();

    public Author(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addPub(String idPub) {
        this.pubs.add(idPub);
    }

    @Override
    public boolean equals(Object outher) {
        return this.id.equals(((Author) outher).getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPubs() {
        return pubs;
    }

    public void setPubs(Set<String> pubs) {
        this.pubs = pubs;
    }



}
