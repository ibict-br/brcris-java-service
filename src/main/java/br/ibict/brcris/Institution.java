package br.ibict.brcris;

import java.util.HashSet;
import java.util.Set;

public class Institution {
    String id;
    String name;
    Set<Author> authors = new HashSet<>();

    public Institution(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object outher) {
        return this.id.equals(((Institution) outher).getId());
    }

    public void addAuthor(Author author) {
        this.authors.add(author);
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

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }



}
