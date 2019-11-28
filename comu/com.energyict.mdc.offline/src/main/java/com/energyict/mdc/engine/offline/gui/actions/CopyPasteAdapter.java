/*
 * CopyPasteAdapter.java
 *
 * Created on 26 januari 2004, 14:15
 *
 * Base found at http://www.javaworld.com/javaworld/javatips/jw-javatip77.html
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.table.TableUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * CopyPasteAdapter enables Copy-Paste Clipboard functionality on JTables.
 * The clipboard data format used by the adapter is compatible with
 * the clipboard format used by Excel. This provides for clipboard
 * interoperability between enabled JTables and Excel.
 * <p/>
 * [2005-aug-31] Print functionality added
 * Ctrl+P prints the table (java 1.5 required)
 */
public class CopyPasteAdapter implements ActionListener {

    private static final Log logger = LogFactory.getLog(CopyPasteAdapter.class);
    private String rowstring, value;
    private Clipboard system;
    private JTable theTable;

    public static final String COPYCOMMAND = "Copy";
    public static final String PASTECOMMAND = "Paste";
    public static final String PRINTCOMMAND = "Print";

    /**
     * The CopyPasteAdapter is constructed with a
     * JTable on which it enables Copy-Paste and acts
     * as a Clipboard listener.
     */

    public CopyPasteAdapter(JTable myJTable) {
        theTable = myJTable;
        // Identifying the copy KeyStroke 
        // you can modify this to copy on some other Key combination:
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        // Identifying the Paste KeyStroke 
        // you can modify this to paste on some other Key combination:
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        // Geert [2004-jun-01] ("for S.G.")
        KeyStroke paste2 = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.CTRL_MASK, false);
        // Identifying the Print KeyStroke
        KeyStroke print = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK, false);

        theTable.registerKeyboardAction(this, COPYCOMMAND, copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        theTable.registerKeyboardAction(this, PASTECOMMAND, paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        theTable.registerKeyboardAction(this, PASTECOMMAND, paste2, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        theTable.registerKeyboardAction(this, PRINTCOMMAND, print, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Remark: pasting doesn't work if a cell of the table is being edited
        // because in that case, the Ctrl+V is 'consumed' by the CellEditor
        // To overcome this, we have to remove the paste-keystroke from the CellEditor
        // cf. cso.renderers.BigDecimalCellEditor, IntegerCellEditor, TextCellEditor

        // 2010-jul-28 (GDE)
        // Remark: copy didn't work if the row was selected by clicking a column having the LargeStringTableCellEditor
        // because in that case, the Ctrl+C is 'consumed' by that editor
        // To overcome this, we removed the copy-keystroke from that editor (cf. LargeStringTableCellEditor)

        system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Public Accessor methods for the Table on which this adapter acts.
     */
    public JTable getJTable() {
        return theTable;
    }

    public void setJTable(JTable theTable) {
        this.theTable = theTable;
    }

    /**
     * This method is activated on the Keystrokes we are listening to
     * in this implementation. Here it listens for Copy and Paste ActionCommands.
     */
    public void actionPerformed(ActionEvent e) {
        TableModel model = theTable.getModel();

        if (e.getActionCommand().equals(COPYCOMMAND)) {
            TableUtils.copySelectedTableData(theTable);
        }

        if (e.getActionCommand().equals(PRINTCOMMAND)) {
            if (!TableUtils.canPrint()) {
                JOptionPane.showMessageDialog(null,
                        TranslatorProvider.instance.get().getTranslator().getTranslation("ctrlPRequiresJre15"));
            } else {
                TableUtils.print(theTable/*, TableUtils.FIT_WIDTH, null, new MessageFormat("EIServer   p.{0}")*/);
            }
        }

        if (e.getActionCommand().equals(PASTECOMMAND)) {
            // Does the clipboard contain just one value or mutliple values?
            boolean oneValue = true;
            String trstring = "";
            StringTokenizer stRow = null, stCol = null;
            try {
                trstring = (String) (system.getContents(this).
                        getTransferData(DataFlavor.stringFlavor));
                stRow = new StringTokenizer(trstring, "\n");
                for (int i = 0; stRow.hasMoreTokens() && oneValue; i++) {
                    rowstring = stRow.nextToken();
                    stCol = new StringTokenizer(rowstring, "\t");
                    for (int j = 0; stCol.hasMoreTokens(); j++) {
                        value = (String) stCol.nextToken();
                        if (i > 0 || j > 0) {
                            oneValue = false;
                            break;
                        }
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                return;
            } catch (IOException ex) {
                return;
            }

            if (theTable.isEditing() && oneValue) { // Busy editing
                Component comp = theTable.getEditorComponent();
                if (comp instanceof JTextComponent) {
                    ((JTextComponent) comp).paste();
                    return;
                }
            }

            int startRow = theTable.getSelectedRow();
            int startCol = theTable.isEditing() ? theTable.getEditingColumn() : 0;

            // Mantis #5522 fix : disable current cell editing, else last value got pasted into edited cell
            TableCellEditor currentCellEditor = theTable.getCellEditor();
            if (currentCellEditor != null) {
                currentCellEditor.stopCellEditing();
            }

            try {
                // GD [2005-mar-25] No sorting while inserting:
                if (model instanceof TableBubbleSortDecorator) {
                    ((TableBubbleSortDecorator) model).setSortOnChange(false);
                }

                trstring = (String) (system.getContents(this).
                        getTransferData(DataFlavor.stringFlavor));
                stRow = new StringTokenizer(trstring, "\n");

                int row = startRow;
                int maxRows = theTable.getRowCount(); // Geert (2005-May-10)
                boolean intelliParse = false;
                for (int i = 0; stRow.hasMoreTokens() && row < maxRows; i++) {
                    int col = startCol;
                    rowstring = stRow.nextToken();

                    String parts[] = rowstring.split("\t");
                    intelliParse = (parts.length > 2); // user pasted more then value + code

                    for (int j = 0; j < parts.length; j++) {
                        value = parts[j];
                        int theCol = j;
                        if (intelliParse) {
                            if (theCol >= theTable.getModel().getColumnCount() ||
                                    !theTable.isCellEditable(row, theCol)) {
                                continue;
                            }
                        } else {
                            // Search for the first editable column
                            while (col < theTable.getColumnCount() &&
                                    !theTable.isCellEditable(row, col)) {
                                col++;
                            }
                            if (col >= theTable.getColumnCount()) {
                                continue;
                            }
                            theCol = col;
                        }
                        TableCellEditor editor = theTable.getCellEditor(row, theCol);
                        Component comp = editor.getTableCellEditorComponent(
                                theTable, theTable.getValueAt(row, theCol), false, row, theCol);
                        if (comp instanceof JLabel) {
                            ((JLabel) comp).setText(value);
                        } else if (comp instanceof JCheckBox) {
                            ((JCheckBox) comp).setSelected(value.equals(
                                    TranslatorProvider.instance.get().getTranslator().getTranslation("yes")));
                        } else if (comp instanceof JTextField) {
                            ((JTextField) comp).setText(value);
                        }
                        theTable.setValueAt(editor.getCellEditorValue(), row, theCol);
                        if (!intelliParse) {
                            col++;
                        }
                    }
                    row++;
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } finally {
                // GD [2005-mar-25] Set the sorting back on + resort:
                if (model instanceof TableBubbleSortDecorator) {
                    ((TableBubbleSortDecorator) model).setSortOnChange(true);
                    ((TableBubbleSortDecorator) model).resort();
                }
            }
        }
    }
}