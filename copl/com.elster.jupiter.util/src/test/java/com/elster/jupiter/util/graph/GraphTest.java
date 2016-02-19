package com.elster.jupiter.util.graph;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GraphTest {

    @Test
    public void testSingleVertexWithSelfReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between("A", "A"));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testTwoVerticesWithMutualReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between("A", "B"));
        graph.addEdge(Edge.between("B", "A"));

        assertThat(graph.isCyclic()).isFalse();
    }

    @Test
    public void testLargerLoopWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between("A", "B"));
        graph.addEdge(Edge.between("B", "C"));
        graph.addEdge(Edge.between("C", "D"));
        graph.addEdge(Edge.between("D", "E"));
        graph.addEdge(Edge.between("E", "F"));
        graph.addEdge(Edge.between("F", "A"));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between("A", "B"));
        graph.addEdge(Edge.between("B", "C"));
        graph.addEdge(Edge.between("C", "D"));

        graph.addEdge(Edge.between("D", "M"));
        graph.addEdge(Edge.between("M", "N"));
        graph.addEdge(Edge.between("N", "O"));
        graph.addEdge(Edge.between("D", "P"));
        graph.addEdge(Edge.between("P", "Q"));
        graph.addEdge(Edge.between("Q", "R"));
        graph.addEdge(Edge.between("R", "O"));


        graph.addEdge(Edge.between("D", "E"));
        graph.addEdge(Edge.between("E", "F"));
        graph.addEdge(Edge.between("F", "A"));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithoutCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between("A", "B"));
        graph.addEdge(Edge.between("B", "C"));
        graph.addEdge(Edge.between("C", "D"));

        graph.addEdge(Edge.between("D", "M"));
        graph.addEdge(Edge.between("M", "N"));
        graph.addEdge(Edge.between("N", "O"));
        graph.addEdge(Edge.between("D", "P"));
        graph.addEdge(Edge.between("P", "Q"));
        graph.addEdge(Edge.between("Q", "R"));

        graph.addEdge(Edge.between("E", "F"));
        graph.addEdge(Edge.between("F", "A"));

        assertThat(graph.isCyclic()).isFalse();
    }
}