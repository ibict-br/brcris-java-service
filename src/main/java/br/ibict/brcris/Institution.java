package br.ibict.brcris;

public class Institution {
    String id;
    String name;

    public Institution(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object outher) {
        return this.id.equals(((Institution) outher).getId());
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



}
