package br.ibict.brcris.model;

import java.util.List;

public class Pqseniorpers {
    List<Orgunit> orgunit;
    List<String> name;
    String id;

    public Pqseniorpers() {}

    public List<Orgunit> getOrgunit() {
        return orgunit;
    }

    public void setOrgunits(List<Orgunit> orgunit) {
        this.orgunit = orgunit;
    }

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


}
