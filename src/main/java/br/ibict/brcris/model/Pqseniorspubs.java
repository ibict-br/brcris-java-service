package br.ibict.brcris.model;

import java.util.List;

public class Pqseniorspubs {
    String id;
    List<PubsAuthors> author;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PubsAuthors> getAuthor() {
        return author;
    }

    public void setAuthor(List<PubsAuthors> author) {
        this.author = author;
    }


}
