/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.HasId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A Chopper is capable of chopping up a set of business objects
 * {@link HasId that have an id} into chunks of at most 'n' elements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-22 (11:15)
 */
public final class Chopper {

    private final Set<? extends HasId> idBusinessObjects;

    public Chopper(Set<? extends HasId> idBusinessObjects) {
        super();
        this.idBusinessObjects = idBusinessObjects;
    }

    public static Chopper chopUp(Set<? extends HasId> idBusinessObjects) {
        return new Chopper(idBusinessObjects);
    }

    public List<List<? extends HasId>> into (int chunckSize) {
        List<List<? extends HasId>> allChunks = new ArrayList<>();
        if (this.idBusinessObjects.size() <= chunckSize) {
            List<HasId> singleChuck = new ArrayList<>(this.idBusinessObjects);
            allChunks.add(singleChuck);
        }
        else {
            List<HasId> currentChunk = new ArrayList<>(chunckSize);
            allChunks.add(currentChunk);
            for (HasId hasId : this.idBusinessObjects) {
                if (currentChunk.size() == chunckSize) {
                    // Current chuck is full, take another one
                    currentChunk = new ArrayList<>(chunckSize);
                    allChunks.add(currentChunk);
                }
                currentChunk.add(hasId);
            }
        }
        return allChunks;
    }

}