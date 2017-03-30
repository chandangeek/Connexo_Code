/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.HasId;

import java.util.List;


public class CollectionUtil {
    public static<H extends HasId> boolean contains(List<H> list ,H object){
        for (H h : list) {
            if(h.getId()==object.getId()){
                return true;
            }
        }
        return false;
    }
}
