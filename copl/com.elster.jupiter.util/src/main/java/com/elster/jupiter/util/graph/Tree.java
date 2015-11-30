package com.elster.jupiter.util.graph;

public class Tree<T> extends DiGraph<T> {

    @Override
    public void addVertex(T data) {
        Node<T> node = Node.of(data);
        //if (vert//)
        super.addVertex(data);
    }

    @Override
    public boolean isCyclic() {
        return super.isCyclic();
    }

    @Override
    public boolean isTree() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
