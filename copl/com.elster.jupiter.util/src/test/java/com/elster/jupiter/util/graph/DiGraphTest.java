package com.elster.jupiter.util.graph;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DiGraphTest {

    @Test
    public void testSingleVertexWithSelfReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge(DiEdge.between(Node.of("A"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testTwoVerticesWithMutualReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge(DiEdge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(DiEdge.between(Node.of("B"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopWithCyclicReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge(DiEdge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(DiEdge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(DiEdge.between(Node.of("C"), Node.of("D")));
        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("E")));
        graph.addEdge(DiEdge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(DiEdge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithCyclicReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge(DiEdge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(DiEdge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(DiEdge.between(Node.of("C"), Node.of("D")));

        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("M")));
        graph.addEdge(DiEdge.between(Node.of("M"), Node.of("N")));
        graph.addEdge(DiEdge.between(Node.of("N"), Node.of("O")));
        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("P")));
        graph.addEdge(DiEdge.between(Node.of("P"), Node.of("Q")));
        graph.addEdge(DiEdge.between(Node.of("Q"), Node.of("R")));
        graph.addEdge(DiEdge.between(Node.of("R"), Node.of("O")));


        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("E")));
        graph.addEdge(DiEdge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(DiEdge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithoutCyclicReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge(DiEdge.between(Node.of("A"), Node.of("B")));
        graph.addEdge(DiEdge.between(Node.of("B"), Node.of("C")));
        graph.addEdge(DiEdge.between(Node.of("C"), Node.of("D")));

        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("M")));
        graph.addEdge(DiEdge.between(Node.of("M"), Node.of("N")));
        graph.addEdge(DiEdge.between(Node.of("N"), Node.of("O")));
        graph.addEdge(DiEdge.between(Node.of("D"), Node.of("P")));
        graph.addEdge(DiEdge.between(Node.of("P"), Node.of("Q")));
        graph.addEdge(DiEdge.between(Node.of("Q"), Node.of("R")));
        graph.addEdge(DiEdge.between(Node.of("R"), Node.of("O")));

        graph.addEdge(DiEdge.between(Node.of("E"), Node.of("F")));
        graph.addEdge(DiEdge.between(Node.of("F"), Node.of("A")));

        assertThat(graph.isCyclic()).isFalse();
    }
}