package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.HasId;

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
