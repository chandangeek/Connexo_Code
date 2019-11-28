/*
 * TreeRefresher.java
 *
 * Created on 23 november 2004, 14:59
 */

package com.energyict.mdc.engine.offline.gui.util;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Geert
 */
public class TreeRefresher {

    // contains the identification string of the nodes to expand
    // identification string = parent node toString() # node toString()
    private Set theSet = new HashSet();
    private String selectedNodeId = "";
    private TreePath pathToReselect;

    /**
     * Creates a new instance of TreeRefresher
     */
    public TreeRefresher() {
    }

    public void takeSnapShot(JTree theTree) {
        theSet.clear();
        TreePath currentSelection = theTree.getSelectionPath();
        selectedNodeId = ((currentSelection == null) ? "" :
                getIdentification((TreeNode) currentSelection.getLastPathComponent()));
        visitAllExpandedNodes(theTree);
    }

    public void applySnapShot(JTree theTree) {
        visitAllNodes(theTree);
    }


    // Traverse all expanded nodes in tree

    private void visitAllExpandedNodes(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        visitAllExpandedNodes(tree, new TreePath(root));
    }

    public void visitAllExpandedNodes(JTree tree, TreePath parent) {
        if (!tree.isExpanded(parent)) {
            return;
        } // node is not expanded

        // node is visible and is visited exactly once
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        remember(node);

        // Visit all children
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                visitAllExpandedNodes(tree, path);
            }
        }
    }

    private void remember(TreeNode node) {
        theSet.add(getIdentification(node));
    }

    // Traverse all nodes in tree

    public void visitAllNodes(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        pathToReselect = null;
        visitAllNodes(tree, root);
        if (pathToReselect != null) {
            tree.setSelectionPath(pathToReselect);
        }
    }

    public void visitAllNodes(JTree tree, TreeNode node) {
        // node is visited exactly once
        apply(tree, node);

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                visitAllNodes(tree, n);
            }
        }
    }

    private void apply(JTree tree, TreeNode node) {
        String identification = getIdentification(node);

        if (identification.equals(selectedNodeId)) {
            pathToReselect = getPath(tree, node);
        }

        if (theSet.contains(identification)) {
            tree.expandPath(getPath(tree, node));
        }
    }

    private TreePath getPath(JTree tree, TreeNode node) {
        Enumeration enumer = null;
        TreeNode rootNode = (TreeNode) tree.getModel().getRoot();
        enumer = ((DefaultMutableTreeNode) node).pathFromAncestorEnumeration(rootNode);
        TreePath path = new TreePath(rootNode);
        DefaultMutableTreeNode aNode = null;
        while (enumer.hasMoreElements()) {
            aNode = (DefaultMutableTreeNode) enumer.nextElement();
            if (!aNode.isRoot()) {
                path = path.pathByAddingChild(aNode);
            }
        }
        return path;
    }

    private String getIdentification(TreeNode node) {
        StringBuffer identification = new StringBuffer();
        TreeNode parent = node.getParent();
        if (parent != null) {
            identification.append(parent.toString());
        }
        identification.append("#");
        identification.append(node.toString());
        return identification.toString();
    }
}
