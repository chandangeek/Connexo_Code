/*
 * RegisterInfo.java
 *
 * Created on 28 mei 2004, 11:41
 */

package com.energyict.protocol;

/**
 * @author Koen
 */
public class RegisterInfo {

    private final String info;

    public RegisterInfo(String info) {
        this.info = info;
    }

    public String toString() {
        return info;
    }
}