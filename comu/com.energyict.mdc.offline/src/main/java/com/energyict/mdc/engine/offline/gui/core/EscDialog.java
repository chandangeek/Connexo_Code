/*
 * EscDialog.java
 *
 * Created on 11 juli 2003, 16:18
 */

package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * a JDialog that appears when ESC is pressed
 *
 * @author Geert
 */
public class EscDialog extends JDialog
        implements ContainerListener, KeyListener {

    public EscDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        addListenersRecursively(this);
    }

    public EscDialog(Frame frame, boolean modal) {
        super(frame, modal);
        addListenersRecursively(this);
    }

    //The following function is recursive and is intended for internal use only.
    //It is private to prevent anyone calling it from other classes
    //The function takes a Component as an argument and adds this JDialog 
    //as a KeyListener to it.
    //Besides it checks if the component is actually a Container and if it is, 
    //there  are 2 additional things to be done to this Container :
    // 1 - add this Dialog as a ContainerListener to the Container
    // 2 - call this function recursively for every child of the Container.

    private void addListenersRecursively(Component c) {
        //To be on the safe side, try to remove KeyListener first 
        //just in case it has been added before.
        //If not, it won't do any harm
        c.removeKeyListener(this);
        //Add KeyListener to the Component passed as an argument
        c.addKeyListener(this);

        if (c instanceof Container) {
            //Component c is a Container. The following cast is safe.
            Container cont = (Container) c;
            //To be on the safe side, try to remove ContainerListener first 
            //just in case it has been added before.
            //If not, it won't do any harm
            cont.removeContainerListener(this);
            //Add ContainerListener to the Container.
            cont.addContainerListener(this);

            //Get the Container's array of children Components.
            Component[] children = cont.getComponents();

            //For every child repeat the above operation.
            for (int i = 0; i < children.length; i++) {
                addListenersRecursively(children[i]);
            }
        }
    }

    //The following function is the same as the function above with the exception that it does exactly the opposite - removes this Dialog
    //from the listener lists of Components.

    private void removeListenersRecursively(Component c) {
        c.removeKeyListener(this);
        if (c instanceof Container) {
            Container cont = (Container) c;
            cont.removeContainerListener(this);
            Component[] children = cont.getComponents();
            for (int i = 0; i < children.length; i++) {
                removeListenersRecursively(children[i]);
            }
        }
    }

    //ContainerListener interface
    //---------------------------------------------------------

    //This function is called whenever a Component or a Container is added 
    //to another Container belonging to this Dialog

    public void componentAdded(ContainerEvent e) {
        addListenersRecursively(e.getChild());
    }

    //This function is called whenever a Component or a Container is removed from another Container belonging to this Dialog

    public void componentRemoved(ContainerEvent e) {
        removeListenersRecursively(e.getChild());
    }

    //KeyListener interface
    //---------------------------------------------------------

    //This function is called whenever a Component belonging to this Dialog 
    //(or the Dialog itself) gets the KEY_PRESSED event

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            //Key pressed is the ESCAPE key.
            performEscapeAction(e);
        }
    }
    //We need the following 2 functions to complete imlementation of KeyListener

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    //Default response to ESCAPE key pressed goes here
    //Redefine this function in subclasses to respond to ENTER key
    //differently

    public void performEscapeAction(KeyEvent evt) {
        setVisible(false);
    }
}
