package com.elster.jupiter.util.graph;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GraphTest {

    @Test
    public void testSingleVertexWithSelfReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between(Node.of("A"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testTwoVerticesWithMutualReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(Edge.between(Node.of("B"), Node.of("A")));

        assertThat(graph.isCyclic()).isFalse();
    }

    @Test
    public void testLargerLoopWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(Edge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(Edge.between(Node.of("C"), Node.of("D")));
        graph.addEdge(Edge.between(Node.of("D"), Node.of("E")));
        graph.addEdge(Edge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(Edge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(Edge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(Edge.between(Node.of("C"), Node.of("D")));

        graph.addEdge(Edge.between(Node.of("D"), Node.of("M")));
        graph.addEdge(Edge.between(Node.of("M"), Node.of("N")));
        graph.addEdge(Edge.between(Node.of("N"), Node.of("O")));
        graph.addEdge(Edge.between(Node.of("D"), Node.of("P")));
        graph.addEdge(Edge.between(Node.of("P"), Node.of("Q")));
        graph.addEdge(Edge.between(Node.of("Q"), Node.of("R")));
        graph.addEdge(Edge.between(Node.of("R"), Node.of("O")));


        graph.addEdge(Edge.between(Node.of("D"), Node.of("E")));
        graph.addEdge(Edge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(Edge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithoutCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge(Edge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(Edge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(Edge.between(Node.of("C"), Node.of("D")));

        graph.addEdge(Edge.between(Node.of("D"), Node.of("M")));
        graph.addEdge(Edge.between(Node.of("M"), Node.of("N")));
        graph.addEdge(Edge.between(Node.of("N"), Node.of("O")));
        graph.addEdge(Edge.between(Node.of("D"), Node.of("P")));
        graph.addEdge(Edge.between(Node.of("P"), Node.of("Q")));
        graph.addEdge(Edge.between(Node.of("Q"), Node.of("R")));

        graph.addEdge(Edge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(Edge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isFalse();
    }
}