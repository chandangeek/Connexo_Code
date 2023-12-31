package com.energyict.mdc.engine.offline.gui.util;

/*
 * JPopupMenuButton.java
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 15. Juli 2005, 10:09
 * 
 * Found @ http://flexo.cismet.de/gadgets/
 *
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * An implementation of a "popup menu button". See a short
 * <a href="http://flexo.cismet.de/gadgets/JPopupMenuButton/">description and demo</a>
 * on the website.
 * <p>
 * <p/>
 * Very easy to use:<br>
 * - Create the Button<br>
 * - Add an icon<br>
 * - Add an popup menu<br>
 * - Add an action event listener for the main action<br>
 * - Add an action event listener for the menu items<br>
 * - Ready<br>
 * </p>
 * <br><br>
 * <p>
 * License: <a href="http://www.gnu.org/copyleft/lesser.html#TOC1">GNU LESSER GENERAL PUBLIC LICENSE</a>
 * <br><img src="http://opensource.org/trademarks/osi-certified/web/osi-certified-60x50.gif"> <img src="http://opensource.org/trademarks/opensource/web/opensource-55x48.gif">
 * </p>
 *
 * @author thorsten.hell@cismet.de
 */
public class JPopupMenuButton extends JButton implements MouseListener, MouseMotionListener {

    protected JPopupMenu popupMenu = null;

    private int arrowXOffset = 0;

    boolean mouseInPopupArea = false;
    Icon downArrow = EisIcons.DOWNSMALL_ICON;
    Icon downArrow2 = EisIcons.DOWNSMALL2_ICON;
    Icon userDefinedIcon = null;

    /**
     * Creates a new instance of JPopupMenuButton
     */
    public JPopupMenuButton() {
        setIcon(null);
        setVerticalTextPosition(SwingConstants.CENTER);
        setHorizontalTextPosition(SwingConstants.LEFT);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setFocusPainted(false);
    }

    /**
     * Returns whether the given point x,y is in the popup area or not.
     */
    private boolean isOverMenuPopupArea(int x, int y) {
        if (x >= getWidth() - getIcon().getIconWidth() + arrowXOffset - getInsets().right && x <= getWidth() - 1) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {
        boolean oldValue = mouseInPopupArea;
        mouseInPopupArea = isOverMenuPopupArea((int) e.getPoint().getX(), (int) e.getPoint().getY());
        if (oldValue != mouseInPopupArea) {
            evaluateIcon();
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     */
    public void mouseDragged(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {
        mouseInPopupArea = false;
        evaluateIcon();
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e) {
        if (this.isEnabled()) {

            if (canShowPopup() && mouseInPopupArea) {
                actionPerformed(new ActionEvent(this, 0, ""));
            } else if (mouseInPopupArea || e.isPopupTrigger()) {
                showPopupMenu(e);
            }
        }
    }

    protected boolean canShowPopup() {
        return (popupMenu == null || popupMenu.getComponentCount() == 0);
    }

    protected void showPopupMenu(MouseEvent e) {
        popupMenu.setVisible(true);
        popupMenu.show(this, e.getX(), e.getY());
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        ActionEvent thisEvent = new ActionEvent(this, 0, "ACTION");
        fireActionPerformed(thisEvent);
    }

    /**
     * Sets the button's default icon. This icon is
     * also used as the "pressed" and "disabled" icon if
     * there is no explicitly set pressed icon.
     *
     * @param defaultIcon the icon used as the default image
     * @beaninfo bound: true
     * attribute: visualUpdate true
     * description: The button's default icon
     * @see #getIcon
     * @see #setPressedIcon
     */
    public void setIcon(Icon defaultIcon) {
        userDefinedIcon = defaultIcon;
        evaluateIcon();
    }


    /**
     * Sets the right down arrow icon
     */
    private void evaluateIcon() {
        if (mouseInPopupArea && isEnabled()) {
            evaluateIcon(downArrow2);
        } else {
            evaluateIcon(downArrow);
        }
    }

    /**
     * Sets the given Icon as down arrow
     *
     * @param arrow the icon used as the arrow
     */
    private void evaluateIcon(Icon arrow) {
        if (userDefinedIcon != null) {
            int newWidth = userDefinedIcon.getIconWidth() + arrow.getIconWidth();
            int newHeight = userDefinedIcon.getIconHeight();
            int arrowYOffset = (userDefinedIcon.getIconHeight() - arrow.getIconHeight()) / 2;
            arrowXOffset = userDefinedIcon.getIconWidth();
            int iconYOffset = 0;
            if (arrow.getIconHeight() > newHeight) {
                newHeight = arrow.getIconHeight();
                arrowYOffset = 0;
                iconYOffset = (arrow.getIconHeight() - userDefinedIcon.getIconHeight()) / 2;
            }
            BufferedImage tmp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            userDefinedIcon.paintIcon(this, tmp.getGraphics(), 0, iconYOffset);
            arrow.paintIcon(this, tmp.getGraphics(), userDefinedIcon.getIconWidth(), arrowYOffset);
            super.setIcon(new ImageIcon(tmp));
        } else {
            super.setIcon(arrow);
        }
    }

    /**
     * Sets the popupmenu
     *
     * @param pop the popup menu
     */
    public void setPopupMenu(JPopupMenu pop) {
        popupMenu = pop;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the <code>event</code>
     * parameter.
     *
     * @param event the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    protected void fireActionPerformed(ActionEvent event) {
        if (popupMenu == null || mouseInPopupArea == false) {
            super.fireActionPerformed(event);
        }
    }


}
