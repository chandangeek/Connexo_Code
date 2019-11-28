package com.energyict.mdc.engine.offline.gui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.plaf.metal.MetalToolTipUI;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

public class MultiLineToolTip extends JToolTip {

    private static final Log logger = LogFactory.getLog(MultiLineToolTip.class);

    public MultiLineToolTip() {
        setUI(new MultiLineToolTipUI());
    }

    private class MultiLineToolTipUI extends MetalToolTipUI {

        private String[] strs;
        private String prevTipText = null;
        private Dimension prevDim;

        public void paint(Graphics g, JComponent c) {
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            Dimension size = c.getSize();
            g.setColor(c.getBackground());
            g.fillRect(0, 0, size.width, size.height);
            g.setColor(c.getForeground());
            if (strs != null) {
                for (int i = 0; i < strs.length; i++) {
                    g.drawString(strs[i], 3, (metrics.getHeight()) * (i + 1));
                }
            }
        }

        public Dimension getPreferredSize(JComponent c) {
            FontMetrics metrics = c.getFontMetrics(c.getFont());
            String tipText = ((JToolTip) c).getTipText();
            if (tipText == null) {
                tipText = "";
            }

            if (prevTipText != null && prevDim != null && prevTipText.equals(tipText)) {
                return prevDim;
            }
            prevTipText = tipText;

            BufferedReader br = new BufferedReader(new StringReader(tipText));
            String line;
            int maxWidth = 0;
            Vector v = new Vector();
            try {
                while ((line = br.readLine()) != null) {
                    int width = SwingUtilities.computeStringWidth(metrics, line);
                    maxWidth = (maxWidth < width) ? width : maxWidth;
                    v.addElement(line);
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
            int lines = v.size();
            if (lines < 1) {
                strs = null;
                lines = 1;
            } else {
                strs = new String[lines];
                int i = 0;
                for (Enumeration e = v.elements(); e.hasMoreElements(); i++) {
                    strs[i] = (String) e.nextElement();
                }
            }
            int height = metrics.getHeight() * lines;
            prevDim = new Dimension(maxWidth + 6, height + 4);
            return prevDim;
        }
    }
}