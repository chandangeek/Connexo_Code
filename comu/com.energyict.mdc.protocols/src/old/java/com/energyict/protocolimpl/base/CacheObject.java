package com.energyict.protocolimpl.base;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 20-mei-2010
 * Time: 10:59:50
 */
public class CacheObject implements Serializable {

    String text;

    public CacheObject(String text){
        this.text = text;
    }

    public String getText(){
        return this.text;
    }

    public void setText(String newText){
        this.text = newText;
    }

}
