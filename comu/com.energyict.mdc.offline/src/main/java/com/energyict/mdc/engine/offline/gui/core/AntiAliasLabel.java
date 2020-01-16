package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.*;
import java.awt.*;

/**
 * Used in logon screen where labels with a big font size are not rendered well
 *
 * @author pdo
 */
public class AntiAliasLabel extends JLabel {

    public AntiAliasLabel(String text) {
        super(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g2);
    }

}

