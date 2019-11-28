package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.core.EscDialog;
import com.energyict.mdc.engine.offline.gui.editors.DateAspectEditor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.EventObject;

/**
 * CellEditor for editing for java.util.Date objects
 */
public class DateCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private Date date;
    private EscDialog popUp;
    private DateAspectEditor editor;
    private JComponent component;
    private JButton button;
    private JButton setButton;

    private MouseEvent mouseEvent; // special case [actionPerformed() not triggered by a click on 'button'] 

    /**
     * Creates a new instance of DateCellEditor
     */
    public DateCellEditor() {
        editor = new DateAspectEditor();
        try {
            editor.init(this, new PropertyDescriptor("date", this.getClass()));
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        setButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("set"));
        setButton.setMnemonic(KeyEvent.VK_S);
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });

        component = new JPanel();
        component.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        component.add(editor.getValueComponent());
        component.add(setButton);

        button = new JButton();
        button.setOpaque(false);
        button.addActionListener(this);      // actionPerformed will be called when clicking twice the column (see isCellEditable())
    }

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    public Object getCellEditorValue() {
        return date;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.date = (Date) value;
        // refresh the view
        editor.setModel(this);
        return button;
    }

    // To have to click twice to edit a date: (copied from DefaultCellEditor)
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return (anEvent instanceof MouseEvent && ((MouseEvent) anEvent).getClickCount() >= 2);
    }

    @Override
    public boolean stopCellEditing() {
        if (!editor.commitEdit()) {
            return false;
        }
        boolean hide = super.stopCellEditing();
        if (hide) {
            if (popUp != null) {
                popUp.setVisible(false);
            }
        }
        return hide;
    }

    @Override
    public void cancelCellEditing() {
        if (popUp != null) {
            popUp.setVisible(false);
        }
        super.cancelCellEditing();
    }

    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Invoked when an button is clicked twice
     */
    public void actionPerformed(ActionEvent e) {
        initPopUp();
        popUp.setVisible(true);
    }

    public void setMouseEvent(MouseEvent e) {
        this.mouseEvent = e;
    }

    private void initPopUp() {
        popUp = new EscDialog(getFrame(), true) {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelCellEditing();
                    super.performEscapeAction(e);
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    stopCellEditing();
                }
            }
        };
        popUp.setUndecorated(true);
        popUp.setContentPane(component);
        popUp.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        popUp.pack();

        if (mouseEvent != null) {
            Point p = mouseEvent.getPoint();
            SwingUtilities.convertPointToScreen(p, (Component) mouseEvent.getSource());
            popUp.setLocation(p);
        } else {
            popUp.setLocationRelativeTo(button);
        }
    }

    private Frame getFrame() {
        return (mouseEvent != null ?
                JOptionPane.getFrameForComponent((Component) mouseEvent.getSource()) :
                JOptionPane.getFrameForComponent(button));
    }
}
