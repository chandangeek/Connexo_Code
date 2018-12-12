/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DiGraphTest {

    @Test
    public void testSingleVertexWithSelfReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge("A", "A");

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testTwoVerticesWithMutualReference() {

        DiGraph<String> graph = new DiGraph<>();

        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertThat(graph.isCyclic()).isTrue();
    }

    @Test
    public void testLargerLoopWithCyclicReference() {

        DiGraph<String> graph = new DiGraph<>();

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

        DiGraph<String> graph = new DiGraph<>();

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
    public void testRemoveVertex() {

        DiGraph<String> graph = new DiGraph<>();

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

        graph.remove("E");

        assertThat(graph.shortestPath("D", "F")).isEmpty();
    }

    @Test
    public void testRemoveEdge() {

        DiGraph<String> graph = new DiGraph<>();

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

        graph.removeEdge("D", "E");

        assertThat(graph.shortestPath("D", "F")).isEmpty();
    }



    @Test
    public void testLargerLoopAndSideTrackWithoutCyclicReference() {

        DiGraph<String> graph = new DiGraph<>();

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
        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        assertThat(graph.isCyclic()).isFalse();
    }

    @Test
    public void testShortestPath() {
        DiGraph<String> graph = new DiGraph<>();

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

        assertThat(graph.shortestPath("Z", "F")).contains(Arrays.asList("Z", "B", "G", "C", "F"));

    }

    @Test
    public void testShortestPath2() {
        DiGraph<String> graph = new DiGraph<>();

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
        DiGraph<String> graph = new DiGraph<>();

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