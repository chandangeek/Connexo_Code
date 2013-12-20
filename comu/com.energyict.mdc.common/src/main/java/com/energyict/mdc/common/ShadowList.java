package com.energyict.mdc.common;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * List of <Code>ShadowObject</Code> keeping track of a newly added, updated and removed Shadows
 *
 * @param <E> ObjectShadow.class
 */
public class ShadowList<E extends ObjectShadow> extends AbstractList<E> implements List<E>, Serializable {

    List<E> shadows = new ArrayList<E>();
    List<E> newShadows = new ArrayList<E>();
    List<E> deletedShadows = new ArrayList<E>();

    /**
     * Creates an empty ShadowList.
     */
    public ShadowList() {
    }

    public ShadowList(Collection<E> in) {
        addAll(in);
    }

    public Iterator<E> iterator() {
        return super.iterator();
    }

    public E get(int index) {
        return shadows.get(index);
    }

    public int size() {
        return shadows.size();
    }

    public E set(int index, E element) {
        if (!deletedShadows.remove(element)) {
            if (!shadows.contains(element)) {
                newShadows.add(element);
            }
        }
        E result = shadows.set(index, element);
        if (result != element) {
            if (!newShadows.remove(result)) {
                deletedShadows.add(result);
            }
        }
        return result;
    }

    /**
     * Adds the {@link ObjectShadow ObjectShadow} to the list without considering
     * the ObjectShadow as a new element in the list.
     *
     * @param newShadow The new ObjectShadow
     */
    public void basicAdd(E newShadow) {
        shadows.add(newShadow);
    }

    /**
     * Forces this ShadowList to remember the specified {@link ObjectShadow ObjectShadow} as updated.
     * Note that the ObjectShadow should already be contained in this ShadowList.
     * The check for containment is done with the == operator.
     *
     * @param dirtyShadow The ObjectShadow that was modified externally
     * @see #basicAdd(ObjectShadow)
     */
    public void notifyUpdated(E dirtyShadow) {
        int shadowIndex = this.shadows.indexOf(dirtyShadow);
        if (shadowIndex > 0) {
            this.set(shadowIndex, dirtyShadow);
            this.newShadows.remove(dirtyShadow);
        }
    }

    /**
     * Replaces the old {@link ObjectShadow ObjectShadow} with a new version,
     * making sure that the status of the ObjectShadow is not modified,
     * i.e. if the old ObjectShadow had status "new", then the new version of the ObjectShadow will also have status "new"
     * or if the old ObjectShadow had status "updated", then the new version will also have status "updated".
     * Note that the old ObjectShadow should already be contained in this ShadowList.
     * The check for containment is done with the == operator.
     *
     * @param oldShadowVersion The old version of the ObjectShadow
     * @param newShadowVersion The new version of the ObjectShadow
     */
    public void replace(E oldShadowVersion, E newShadowVersion) {
        int shadowIndex = this.newShadows.indexOf(oldShadowVersion);
        if (shadowIndex >= 0) {
            this.newShadows.set(shadowIndex, newShadowVersion);
        }
        shadowIndex = this.shadows.indexOf(oldShadowVersion);
        if (shadowIndex >= 0) {
            this.shadows.set(shadowIndex, newShadowVersion);
        }
    }

    // method to support cloning operations

    public void makeAllNew() {
        newShadows = new ArrayList<E>(shadows);
    }

    public void addToEnd(E element) {
        if (!deletedShadows.remove(element)) {
            newShadows.add(element);
        }
        shadows.add(element);
    }

    public void add(int index, E element) {
        if (!deletedShadows.remove(element)) {
            newShadows.add(element);
        }
        shadows.add(index, element);
    }

    public E remove(int index) {
        E result = shadows.remove(index);
        if (!newShadows.remove(result)) {
            deletedShadows.add(result);
        }
        return result;
    }

    public boolean remove(E object) {
        if (!shadows.remove(object)) {
            return false;
        } // is not in
        if (!newShadows.remove(object)) {
            deletedShadows.add(object);
        }
        return true;
    }
    /**
     * @return a list of newly added shadows
     */
    public List<E> getNewShadows() {
        return Collections.unmodifiableList(newShadows);
    }

    /**
     * @return a list of shadows that were added and modified
     */
    public List<E> getDirtyNewShadows(){
        List<E> dirtyShadows = new ArrayList<>();
        for (E each: shadows){
            if (each.isDirty()){
                dirtyShadows.add(each);
            }
        }
        return Collections.unmodifiableList(dirtyShadows);
    }
    /**
     * @return a list of removed shadows
     */
    public List<E> getDeletedShadows() {
        return Collections.unmodifiableList(deletedShadows);
    }

    public List<E> getRemainingShadows() {
        List<E> result = new ArrayList<E>(shadows);
        result.removeAll(newShadows);
        return result;
    }
    /**
     * @return a list of updated shadows
     */
    public List<E> getUpdatedShadows() {
        List<E> result = getRemainingShadows();
        Iterator<E> it = result.iterator();
        while (it.hasNext()) {
            ObjectShadow each = it.next();
            if (!each.isDirty()) {
                it.remove();
            }
        }
        return result;
    }

    public boolean isDirty() {
        return !(newShadows.isEmpty() && deletedShadows.isEmpty() && !hasDirtyShadows());
    }

    public void markClean() {
        newShadows.clear();
        deletedShadows.clear();
        for (ObjectShadow each : getRemainingShadows()) {
            each.markClean();
        }
    }

    private boolean hasDirtyShadows() {
        for (ObjectShadow each : getRemainingShadows()) {
            if (each.isDirty()) {
                return true;
            }
        }
        return false;
    }

    public void swap(int index1, int index2) {
        E shadow1 = shadows.get(index1);
        E shadow2 = shadows.get(index2);
        shadows.remove(index1);
        shadows.add(index1, shadow2);
        shadows.remove(index2);
        shadows.add(index2, shadow1);
    }

    public void prepareCloning() {
        for (ObjectShadow shadow : shadows) {
            shadow.prepareCloning();
        }
    }
}