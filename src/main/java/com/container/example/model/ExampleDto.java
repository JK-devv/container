package com.container.example.model;

public class ExampleDto {
    private long id;
    private String fullName;
    private String printName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPrintName() {
        return printName;
    }

    public void setPrintName(String printName) {
        this.printName = printName;
    }

    @Override
    public String toString() {
        return "ExampleDto{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", printName='" + printName + '\'' +
                '}';
    }
}
