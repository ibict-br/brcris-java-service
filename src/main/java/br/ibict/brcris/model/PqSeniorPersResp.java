package br.ibict.brcris.model;

import java.util.List;

public class PqSeniorPersResp {
    List<Institution> orgunit;
    List<String> name;
    String id;

    public PqSeniorPersResp() {}

    public List<Institution> getOrgunit() {
        return orgunit;
    }

    public void setOrgunits(List<Institution> orgunit) {
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
