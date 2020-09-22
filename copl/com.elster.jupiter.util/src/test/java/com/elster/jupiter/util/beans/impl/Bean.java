/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

public class Bean {
    private int age;
    private String name;
    public String abyss = "You see yourself";

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThirteen() {
        return 13;
    }

    public void setMoniker(String moniker) {
        this.name = moniker;
    }
}
