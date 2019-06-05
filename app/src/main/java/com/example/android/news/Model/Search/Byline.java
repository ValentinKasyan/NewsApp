
package com.example.android.news.Model.Search;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Byline {

    @SerializedName("original")
    @Expose
    private Object original;
    @SerializedName("person")
    @Expose
    private List<Person> person = null;
    @SerializedName("organization")
    @Expose
    private Object organization;

    public Object getOriginal() {
        return original;
    }

    public void setOriginal(Object original) {
        this.original = original;
    }

    public List<Person> getPerson() {
        return person;
    }

    public void setPerson(List<Person> person) {
        this.person = person;
    }

    public Object getOrganization() {
        return organization;
    }

    public void setOrganization(Object organization) {
        this.organization = organization;
    }

}
