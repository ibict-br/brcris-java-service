package br.ibict.brcris.model;

import java.util.List;

public class Author {

    List<String> name;
    String id;

    public Author() {}

    public Author(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object outher) {
        return this.id.equals(((Author) outher).getId());
    }

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
