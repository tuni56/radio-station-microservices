package com.radiostation.program.domain;

public class Program {

    private String id;
    private String name;

    public Program() {}

    public Program(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
