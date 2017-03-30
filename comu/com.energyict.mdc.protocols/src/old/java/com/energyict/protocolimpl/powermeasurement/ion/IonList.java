/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class IonList extends IonObject implements List  {

    List list = new ArrayList();

    IonList() {
        this.type = "IonList";
    }

    void add(IonObject ionObject) {
        list.add(ionObject);
    }
    
    public Iterator iterator( ) {
        return list.iterator();
    }

    public void add(int index, Object element) {
        list.add(index, element);
    }

    public boolean add(Object o) {
        return list.add(o);
    }

    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    public boolean equals(Object o) {
        return list.equals(o);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int hashCode() {
        return list.hashCode();
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    public int size() {
        return list.size();
    }

    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }
 
    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( type + " [" );
        Iterator i = list.iterator();
        while (i.hasNext())
            rslt.append( i.next() + "\n");
        rslt.append( " ]" );
        return rslt.toString();
    }
    
}