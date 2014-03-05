package com.elster.jupiter.http.whiteboard;

/**
 * Copyrights EnergyICT
 * Date: 5/03/14
 * Time: 10:56
 */
public class Script {
    private String name;
    private String path;

    public Script(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
