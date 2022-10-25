package br.ibict.brcris.response;

import java.util.List;

public class PqSeniorsPubsResp {
    String id;
    List<Author> author;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Author> getAuthor() {
        return author;
    }

    public void setAuthor(List<Author> author) {
        this.author = author;
    }


}
