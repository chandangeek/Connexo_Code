package com.energyict.mdc.engine.offline.gui.util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Author: Steven M
 * Utility class for working with JTree
 */
public class JTreeUtil {

    /**
     * Returns the TreePath of the node specified by the array of names
     * Finds the path in tree as specified by the array of names. The names array is a
     * sequence of names where names[0] is the root and names[i] is a child of names[i-1].
     * Comparison is done using String.equals(). Returns null if not found.
     *
     * @param tree  the tree which contains the nodes
     * @param names array of names to look for
     * @return the TreePath if found, null if not found
     */
    public static TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(new TreePath(root), names, 0, true);
    }

    /**
     * Returns an Enumeration of the expansion state for the given tree
     *
     * @param tree the tree for which you want to get the expansion state
     * @return Enumerarion with the expansion stae
     */
    public static Collection<TreePath> saveExpansionState(JTree tree) {
        Collection<TreePath> result = new ArrayList<TreePath>();
        Enumeration<TreePath> treePath = tree.getExpandedDescendants(tree.getPathForRow(0));
        if (treePath != null) {
            while (treePath.hasMoreElements()) {
                result.add(treePath.nextElement());
            }
        }

        return result;
    }

    /**
     * Restore the expansion state of a JTree.
     *
     * @param tree
     * @param collTreePath a collection with the expansion state
     */
    public static void loadExpansionState(JTree tree, Collection<TreePath> collTreePath) {
        collapseAll(tree);

        Iterator it = collTreePath.iterator();
        if (it != null) {
            while (it.hasNext()) {
                TreePath treePath = (TreePath) it.next();
                String treePathString = treePath.toString().replace("[", "").replace("]", "");
                TreePath newPath = JTreeUtil.findByName(tree, treePathString.split(", "));
                if (newPath != null) {
                    tree.expandPath(newPath);
                }
            }
        }
    }

    /**
     * Expand alle nodes in the given tree
     *
     * @param tree
     */
    public static void expandAll(JTree tree) {
        expandCollapseAll(tree, true);
    }

    /**
     * Collapses all nodes in the given tree
     *
     * @param tree
     */
    public static void collapseAll(JTree tree) {
        expandCollapseAll(tree, false);
    }

    //******************************** PRIVATE HELPER METHODS ***********************************//

    private static TreePath find2(TreePath parent, Object[] nodes, int depth, boolean byName) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        Object o = node;

        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }

        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length - 1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find2(path, nodes, depth + 1, byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // No match at this branch
        return null;
    }

    private static void expandCollapseAll(JTree tree, boolean expand) {
        // If expand is true, expands all nodes in the tree.
        // Otherwise, collapses all nodes in the tree.
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }


    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

}
