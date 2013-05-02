package com.elster.jupiter.orm.query.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

class JoinTreeIterator implements Iterator<JoinTreeNode<?>> {
	private final Deque<Iterator<JoinTreeNode<?>>> stack = new ArrayDeque<>();
	private JoinTreeNode<?> nextNode;
	
	public JoinTreeIterator(JoinTreeNode<?> root ) {
		nextNode = root;
		stack.push(root.getChildren().iterator());
	}

	@Override
	public boolean hasNext() {
		return nextNode != null;
	}

	@Override
	public JoinTreeNode<?> next() {
		if (nextNode == null) {
			throw new NoSuchElementException();
		}
		JoinTreeNode<?> result = nextNode;
		advance();
		return result;
	}
	
	private void advance() {		
		Iterator<JoinTreeNode<?>> iterator = stack.peek();
		if (iterator == null) {
			nextNode = null;
		}
		if (iterator.hasNext()) {
			nextNode = iterator.next();
			stack.push(nextNode.getChildren().iterator());
		} else {
			stack.pop();
			advance();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();		
	}

}
