package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.popup.JidePopup;
import com.jidesoft.status.ButtonStatusBarItem;
import com.jidesoft.status.LabelStatusBarItem;
import com.jidesoft.status.ResizeStatusBarItem;
import com.jidesoft.status.StatusBar;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * TableCellEditor that can be used to display and edit large strings.
 * The JTextComponent given as parameter for the constructor is then used as editorComponent
 * of the CellEditor. the editor becomes available by doubleclick it or clicking the '...' button
 * <p/>
 * This editor was not placed in the com.energyict.cso.editors package because
 * it uses a JidePopup as container for the editorComponent
 *
 * @author pdo
 */
public class LargeStringTableCellEditor extends DefaultCellEditor {

    private final static int DEFAULT_ROWS = 12;

    private final JPanel container = new JPanel(new BorderLayout());
    private final JButton button = new JButton();
    private final JidePopup popup = new JidePopup();
    private JScrollPane textComponentScrollPane;

    private JTextComponent textComponent;

    private int rows = DEFAULT_ROWS;


    /**
     * Constructs a new empty LargeStringTableCellEditor that will use
     * the given JTextComponent editComponent as component to edit the large String
     *
     * @param editComponent JTextComponent to be used to edit the large String.
     *                      Typically you will make a choice between a JTextArea, a JEditorPane or a JTextPane
     *                      Using a JTextField has no sense: use the DefaultCellEditor() then.
     * @throws IllegalArgumentException if the JTextComponent is a JTextField
     */
    public LargeStringTableCellEditor(JTextComponent editComponent) {
        super(createEditor());
        if (editComponent instanceof JTextField) {
            throw new IllegalArgumentException("A TextField cannot be used as editorComponent for this TableCellEditor");
        }
        this.textComponent = editComponent;
        initComponents();
    }

    /**
     * Sets the number of rows for the editorComponent.
     *
     * @param rows the number of rows >= 0
     * @throws IllegalArgumentException if rows is less than 0
     * @see #getRows
     */
    public void setRows(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException("rows less than zero.");
        }
        this.rows = rows;
    }

    /**
     * Returns the number of rows in the editorComponent.
     *
     * @return the number of rows >= 0
     */
    public int getRows() {
        return rows;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {

        super.getTableCellEditorComponent(table, value, isSelected, row, column);

        this.editorComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
        this.editorComponent.setForeground(UIManager.getColor("Table.selectionForeground"));

        // EISERVERSG-436 - Added 'description' in alarm rules overview panel
        // The following line of code was responsible for the fact that new lines ('\n')
        // in multi line descriptions were lost:
        //     textComponent.setText(((JTextField) editorComponent).getText());
        // So, I replaced the above line of code with:
        textComponent.setText( value==null ? "" : value.toString() );

        TableColumn col = table.getColumnModel().getColumn(column);
        popup.setPreferredPopupSize(new Dimension(col.getWidth(), table.getRowHeight() * getRows()));
        return container;
    }

    public JButton getButton() {
        return button;
    }

    private void initComponents() {
        ((JTextField) editorComponent).setMargin(new Insets(0, 0, 0, 0));
        ((JTextField) editorComponent).setEditable(false);

        button.setText("...");
        button.setMargin(new Insets(0, 2, 0, 2));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (popup.isPopupVisible()) {
                    popup.setFocusable(false);
                    popup.hidePopup();
                } else {
                    popup.showPopup(container);
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    textComponentScrollPane.getViewport().setViewPosition(new Point(0, 0));
                                }
                            }
                    );
                }
            }
        });

        container.setOpaque(false);
        container.setLayout(new BorderLayout());
        container.add(editorComponent, BorderLayout.CENTER);
        container.add(button, BorderLayout.EAST);

        initPopup();
    }

    private void initPopup() {
        popup.setMovable(false);
        popup.setResizable(true);
        popup.addExcludedComponent(button);
        popup.setDefaultFocusComponent(textComponent);
        // a 'attached popup does not have a resizable border, here we force this
        popup.setPopupBorder(BorderFactory.createCompoundBorder(
                UIDefaultsLookup.getBorder("Resizable.resizeBorder"),
                new EmptyBorder(2, 2, 2, 2)));  // Made a compoundBorder for having more 'grip'
        popup.setupResizeCorner(SwingConstants.SOUTH_EAST);

        JPanel panel = new JPanel(new BorderLayout());
        textComponentScrollPane = new JScrollPane(textComponent);
        panel.add(textComponentScrollPane, BorderLayout.CENTER);
        panel.add(getResizeGripComponent(), BorderLayout.SOUTH);

        popup.setContentPane(panel);
        if (textComponent.isEnabled()) {
            popup.setDefaultFocusComponent(textComponent);
        }

    }

    private JComponent getResizeGripComponent() {
        // The StatusBar is only added to the popup as "resize grip" container
        // This to give to user a visual indication that the popup is resizable;
        StatusBar statusBar = new StatusBar(); // Only used to have a grip to resize the editor
        LabelStatusBarItem filler = new LabelStatusBarItem();
        ResizeStatusBarItem grip = new ResizeStatusBarItem(false); // 

        statusBar.add(filler, JideBoxLayout.VARY);  // as a Filler
        ButtonStatusBarItem okButton = new ButtonStatusBarItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((JTextField) editorComponent).setText(textComponent.getText());
                stopCellEditing();
            }
        });
        okButton.setIcon(EisIcons.CONFIRM_ICON);
        okButton.setToolTip(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        if (textComponent.isEditable()) {
            statusBar.add(okButton, JideBoxLayout.FIX);
        }
        ButtonStatusBarItem cancelButton = new ButtonStatusBarItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
        cancelButton.setIcon(EisIcons.DELETE_ICON);
        cancelButton.setToolTip(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        if (textComponent.isEditable()) {
            statusBar.add(cancelButton, JideBoxLayout.FIX);
        }
        statusBar.add(grip, JideBoxLayout.FIX);

        popup.addExcludedComponent(statusBar);
        popup.addExcludedComponent(filler);
        popup.addExcludedComponent(okButton);
        popup.addExcludedComponent(cancelButton);
        popup.addExcludedComponent(grip);

        return statusBar;
    }

    static private JTextField createEditor() {
        JTextField editor = new JTextField();
        // (2010-jul-28) Geert
        // For this component: remove the normal copy interception
        // Let the JTable this component is in decide what to do on Ctrl+C
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        Action noAction = new AbstractAction() {
            public boolean isEnabled() {
                return false;
            }

            public void actionPerformed(ActionEvent e) {
            }
        };
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(copy, "none");
        editor.getActionMap().put("none", noAction);
        return editor;
    }
}
