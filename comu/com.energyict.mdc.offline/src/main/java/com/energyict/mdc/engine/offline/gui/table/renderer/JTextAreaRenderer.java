/*
 * JTextAreaRenderer.java
 *
 * Created on 27 juli 2004, 15:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Koen
 */
public class JTextAreaRenderer extends JTextArea implements TableCellRenderer {

    private static final Log logger = LogFactory.getLog(JTextAreaRenderer.class);

    /**
     * Creates a new instance of jTextAreaRenderer
     */
    public JTextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(final JTable jTable,
                                                   final Object obj, boolean isSelected, boolean hasFocus, final int row,
                                                   int column) {
        JTextArea jTextArea = (JTextArea) obj;
        setText(jTextArea.getText());

        int height_wanted = (int) getPreferredSize().getHeight();


        if (hasFocus) {
            setBackground(new Color(255, 255, 255));
        } else {
            setBackground(new Color(220, 220, 220));
        }

        if (height_wanted > jTable.getRowHeight(row)) {
            jTable.setRowHeight(row, height_wanted);
        }

        jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                eventKeyTyped(evt, jTable, row, obj);
            }
        });
        return this;
    }

    private void eventKeyTyped(KeyEvent evt, JTable jTable, int row, Object obj) {
        //if (evt.getKeyChar() == '\n') {
        JTextArea jTextArea = (JTextArea) obj;
        setText(jTextArea.getText());

        // ?? voor de line wrap?? jTextArea.validate();

        int height_wanted = (int) getPreferredSize().getHeight();
        logger.debug("KV_DEBUG> eventKeyTyped() --> height wanted=" + height_wanted + ", rowheight=" + jTable.getRowHeight(row));
        if (height_wanted != jTable.getRowHeight(row)) {
            jTable.setRowHeight(row, height_wanted);
        }
        //}
    }

}
