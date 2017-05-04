/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import java.io.Serializable;

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
