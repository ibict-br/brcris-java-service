package br.ibict.brcris.model;

import java.util.List;

public class Organization {
    List<String> name;
    String id;

    public List<String> getName() {
        return name;
    }

    public void setNames(List<String> name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Organization [name=" + name + ", id=" + id + "]";
    }


    
}
