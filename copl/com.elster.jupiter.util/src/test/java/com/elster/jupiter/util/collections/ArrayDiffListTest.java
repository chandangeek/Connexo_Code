/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayDiffListTest {

    @Test
    public void testRemovalsAfterRemoving() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.getRemovals()).hasSize(1).contains("A");
    }

    @Test
    public void testAdditionsAfterRemoving() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.getAdditions()).isEmpty();
    }

    @Test
    public void testRemainingAfterRemoving() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.getRemaining()).hasSize(1).contains("B");
    }

    @Test
    public void testRemovalsAfterAdding() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.getRemovals()).isEmpty();
    }

    @Test
    public void testAdditionsAfterAdding() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.getAdditions()).hasSize(1).contains("C");
    }

    @Test
    public void testRemainingAfterAdding() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.getRemaining()).hasSize(2).contains("A", "B");
    }

    @Test
    public void testRemovalsAfterAddingDuplicate() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.getRemovals()).isEmpty();
    }

    @Test
    public void testAdditionsAfterAddingDuplicate() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.getAdditions()).hasSize(1).contains("A");
    }

    @Test
    public void testRemainingAfterAddingDuplicate() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.getRemaining()).hasSize(2).contains("A", "B");
    }

    @Test
    public void testHasChangedFalse() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        assertThat(diffList.hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedFalseIfRemovalIsUndone() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");
        diffList.add("A");

        assertThat(diffList.hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedFalseIfAdditionIsUndone() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");
        diffList.remove("A");

        assertThat(diffList.hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedTrueWhenItemAdded() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.hasChanged()).isTrue();
    }

    @Test
    public void testHasChangedTrueWhenItemRemoved() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.hasChanged()).isTrue();
    }

    @Test
    public void testHasChangedTrueWhenItemAddedAsOriginal() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.addAsOriginal("C");

        assertThat(diffList.hasChanged()).isFalse();
    }

    @Test
    public void testRemoveThroughIteratorTriggersHasChanged() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.hasChanged()).isTrue();
        assertThat(diffList.getRemovals()).hasSize(1).contains("A");
        assertThat(diffList.getRemaining()).hasSize(1).contains("B");
    }

    @Test
    public void testRemoveThroughIteratorUpdatesRemovals() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.getRemovals()).hasSize(1).contains("A");
    }

    @Test
    public void testRemoveThroughIteratorUpdatesRemaining() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.hasChanged()).isTrue();
        assertThat(diffList.getRemovals()).hasSize(1).contains("A");
        assertThat(diffList.getRemaining()).hasSize(1).contains("B");
    }

    @Test
    public void testRemovalsAfterRemovingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.immutableView().getRemovals()).hasSize(1).contains("A");
    }

    @Test
    public void testAdditionsAfterRemovingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.immutableView().getAdditions()).isEmpty();
    }

    @Test
    public void testRemainingAfterRemovingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.immutableView().getRemaining()).hasSize(1).contains("B");
    }

    @Test
    public void testRemovalsAfterAddingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.immutableView().getRemovals()).isEmpty();
    }

    @Test
    public void testAdditionsAfterAddingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.immutableView().getAdditions()).hasSize(1).contains("C");
    }

    @Test
    public void testRemainingAfterAddingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("C");

        assertThat(diffList.immutableView().getRemaining()).hasSize(2).contains("A", "B");
    }

    @Test
    public void testRemovalsAfterAddingDuplicateOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.immutableView().getRemovals()).isEmpty();
    }

    @Test
    public void testAdditionsAfterAddingDuplicateOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.immutableView().getAdditions()).hasSize(1).contains("A");
    }

    @Test
    public void testRemainingAfterAddingDuplicateOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.immutableView().getRemaining()).hasSize(2).contains("A", "B");
    }

    @Test
    public void testHasChangedFalseOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        assertThat(diffList.immutableView().hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedFalseIfRemovalIsUndoneOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");
        diffList.add("A");

        assertThat(diffList.immutableView().hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedFalseIfAdditionIsUndoneOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");
        diffList.remove("A");

        assertThat(diffList.immutableView().hasChanged()).isFalse();
    }

    @Test
    public void testHasChangedTrueWhenItemAddedOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.add("A");

        assertThat(diffList.immutableView().hasChanged()).isTrue();
    }

    @Test
    public void testHasChangedTrueWhenItemRemovedOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.remove("A");

        assertThat(diffList.immutableView().hasChanged()).isTrue();
    }

    @Test
    public void testHasChangedTrueWhenItemAddedAsOriginalOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        diffList.addAsOriginal("C");

        assertThat(diffList.immutableView().hasChanged()).isFalse();
    }

    @Test
    public void testRemoveThroughIteratorTriggersHasChangedOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.immutableView().hasChanged()).isTrue();
    }

    @Test
    public void testRemoveThroughIteratorUpdatesRemovalsOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.immutableView().getRemovals()).hasSize(1).contains("A");
    }

    @Test
    public void testRemoveThroughIteratorUpdatesRemainingOnImmutableView() {
        DiffList<String> diffList = ArrayDiffList.fromOriginal(Arrays.asList("A", "B"));

        Iterator<String> iterator = diffList.iterator();
        iterator.next();
        iterator.remove();

        assertThat(diffList.immutableView().getRemaining()).hasSize(1).contains("B");
    }


}
