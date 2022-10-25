package br.ibict.brcris.model;

import java.util.List;

public class Author {
    List<String> name;
    String id;

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
