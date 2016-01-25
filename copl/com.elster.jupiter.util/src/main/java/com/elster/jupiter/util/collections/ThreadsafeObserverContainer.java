package com.elster.jupiter.util.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ThreadSafeObserverContainer<T> implements ObserverContainer<T> {

    private List<T> observers = new CopyOnWriteArrayList<T>();

    @Override
    public Subscription subscribe(T observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
        return () -> observers.remove(observer);
    }

    @Override
    public void notify(Consumer<T> notification) {
        observers.forEach(notification);
    }
}
