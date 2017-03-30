/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GraphTest {

    @Test
    public void testSingleVertexWithSelfReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "A");

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testTwoVerticesWithMutualReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertThat(graph.isCyclic()).isFalse();
    }

    @Test
    public void testLargerLoopWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");
        graph.addEdge("D", "E");
        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        graph.addEdge("D", "M");
        graph.addEdge("M", "N");
        graph.addEdge("N", "O");
        graph.addEdge("D", "P");
        graph.addEdge("P", "Q");
        graph.addEdge("Q", "R");
        graph.addEdge("R", "O");


        graph.addEdge("D", "E");
        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopAndSideTrackWithoutCyclicReference() {

        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        graph.addEdge("D", "M");
        graph.addEdge("M", "N");
        graph.addEdge("N", "O");
        graph.addEdge("D", "P");
        graph.addEdge("P", "Q");
        graph.addEdge("Q", "R");

        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        assertThat(graph.isCyclic()).isFalse();
    }

    @Test
    public void testConnected() {
        Graph<String> graph = new Graph<>();
        graph.addEdge("A", "B");

        graph.addVertex("C");

        assertThat(graph.isConnected()).isFalse();
    }

    @Test
    public void testPathToSelfExists() {
        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        graph.addEdge("D", "M");
        graph.addEdge("M", "N");
        graph.addEdge("N", "O");
        graph.addEdge("D", "P");
        graph.addEdge("P", "Q");
        graph.addEdge("Q", "R");

        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        assertThat(graph.shortestPath("A", "A")).isPresent();
    }

    @Test
    public void testShortestPath() {
        Graph<String> graph = new Graph<>();

        graph.addEdge("D", "E", 35);
        graph.addEdge("E", "D", 35);
        graph.addEdge("D", "G", 37);
        graph.addEdge("E", "G", 28);
        graph.addEdge("G", "E", 28);
        graph.addEdge("E", "A", 32);
        graph.addEdge("Z", "D", 38);
        graph.addEdge("Z", "B", 26);
        graph.addEdge("G", "C", 39);
        graph.addEdge("A", "C", 29);
        graph.addEdge("B", "G", 34);
        graph.addEdge("F", "B", 40);
        graph.addEdge("C", "F", 52);
        graph.addEdge("F", "Z", 58);
        graph.addEdge("F", "D", 93);

        assertThat(graph.shortestPath("Z", "F")).contains(Arrays.asList("Z", "F"));

    }

    @Test
    public void testShortestPath4() {
        Graph<String> graph = new Graph<>();

        graph.addEdge("D", "E", 35);
        graph.addEdge("E", "D", 35);
        graph.addEdge("D", "G", 37);
        graph.addEdge("E", "G", 28);
        graph.addEdge("G", "E", 28);
        graph.addEdge("E", "A", 32);
        graph.addEdge("Z", "D", 38);
        graph.addEdge("Z", "B", 26);
        graph.addEdge("G", "C", 39);
        graph.addEdge("A", "C", 29);
        graph.addEdge("B", "G", 34);
        graph.addEdge("F", "B", 40);
        graph.addEdge("C", "F", 52);
        graph.addEdge("F", "D", 93);

        assertThat(graph.shortestPath("Z", "F")).contains(Arrays.asList("Z", "B", "F"));

    }

    @Test
    public void testShortestPath2() {
        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B", 5);
        graph.addEdge("A", "C", 1);
        graph.addEdge("C", "D", 1);
        graph.addEdge("D", "E", 1);
        graph.addEdge("E", "F", 1);
        graph.addEdge("F", "G", 1);
        graph.addEdge("G", "B", 1);

        assertThat(graph.shortestPath("A", "B")).contains(Arrays.asList("A", "B"));

    }

    @Test
    public void testShortestPath3() {
        Graph<String> graph = new Graph<>();

        graph.addEdge("A", "B", 7);
        graph.addEdge("A", "C", 1);
        graph.addEdge("C", "D", 1);
        graph.addEdge("D", "E", 1);
        graph.addEdge("E", "F", 1);
        graph.addEdge("F", "G", 1);
        graph.addEdge("G", "B", 1);

        assertThat(graph.shortestPath("A", "B")).contains(Arrays.asList("A", "C", "D", "E", "F", "G", "B"));

    }

}