package com.energyict.mdc.engine.offline.gui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class JLinkButton extends JButton {

    final Color COLOR_HOVER = Color.BLUE;
    private Color lineColorNormal = Color.LIGHT_GRAY;
    private Color defaultForeground;
    private Color lineColor = lineColorNormal;

    private boolean alwaysUseLinkColor = false;

    public JLinkButton() {
        this(false);
    }

    public JLinkButton(boolean alwaysUseLinkColor) {
        this.alwaysUseLinkColor = alwaysUseLinkColor;
        addMouseListener();
        lineColorNormal = new JButton().getBackground();
        lineColor = lineColorNormal;
        if (alwaysUseLinkColor) {
            switchToLinkColor();
        }
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(lineColor);
        g.drawLine(2, getHeight() - 2, (int) getPreferredSize().getWidth() - 2, getHeight() - 2);
    }

    public void addMouseListener() {

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent me) {
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mousePressed(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
                if (isEnabled()) {
                    if (!alwaysUseLinkColor) {
                        switchToLinkColor();
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            public void mouseExited(MouseEvent me) {
                if (isEnabled()) {
                    if (!alwaysUseLinkColor) {
                        switchToDefaultColor();
                    }
                    setCursor(Cursor.getDefaultCursor());
                }
            }

        });
    }

    private void switchToLinkColor() {
        defaultForeground = getForeground();
        setForeground(COLOR_HOVER);
        lineColor = COLOR_HOVER;
    }

    private void switchToDefaultColor() {
        setForeground(defaultForeground);
        lineColor = lineColorNormal;
    }
}