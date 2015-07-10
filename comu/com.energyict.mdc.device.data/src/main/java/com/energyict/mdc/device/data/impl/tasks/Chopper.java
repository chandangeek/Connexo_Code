package com.energyict.mdc.device.data.impl.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Chopper is capable of chopping up a set of objects into chunks of at most 'n' elements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-22 (11:15)
 */
public final class Chopper<T> {

    private final Collection<T> objects;

    public Chopper(Collection<T> objects) {
        super();
        this.objects = objects;
    }

    public static <T> Chopper<T> chopUp(Collection<T> objects) {
        return new Chopper(objects);
    }

    public List<List<T>> into (int chunkSize) {
        List<List<T>> allChunks = new ArrayList<>();
        if (this.objects.size() <= chunkSize) {
            List<T> singleChuck = new ArrayList<>(this.objects);
            allChunks.add(singleChuck);
        } else {
            List<T> currentChunk = new ArrayList<>(chunkSize);
            allChunks.add(currentChunk);
            for (T obj : this.objects) {
                if (currentChunk.size() == chunkSize) {
                    // Current chuck is full, take another one
                    currentChunk = new ArrayList<>(chunkSize);
                    allChunks.add(currentChunk);
                }
                currentChunk.add(obj);
            }
        }
        return allChunks;
    }
}