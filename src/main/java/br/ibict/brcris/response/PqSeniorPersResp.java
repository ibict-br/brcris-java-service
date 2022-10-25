package br.ibict.brcris.response;

import java.util.ArrayList;
import java.util.List;

public class PqSeniorPersResp {
    List<Organization> orgunit = new ArrayList<>();
    List<String> name;
    String id;

    public PqSeniorPersResp() {}

    public List<Organization> getOrgunit() {
        return orgunit;
    }

    public void setOrgunits(List<Organization> orgunit) {
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

    @Override
    public String toString() {
        return "PqSeniorPersResp [orgunit=" + orgunit + ", name=" + name + ", id=" + id + "]";
    }



}
