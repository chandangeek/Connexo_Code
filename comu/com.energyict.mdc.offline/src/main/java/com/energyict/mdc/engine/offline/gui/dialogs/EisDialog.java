/*
 * EisDialog.java
 *
 * Created on April 29, 2003, 8:42 AM
 * Found at http://www.javaworld.com/javaworld/javatips/jw-javatip69.html
 * (Adapted to be a JDialog instead of a Dialog)
 */

package com.energyict.mdc.engine.offline.gui.dialogs;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;

/**
 * @author Geert
 *         ==> Dialog that closes whenever you press ESC
 */
public class EisDialog extends JDialog implements ContainerListener, KeyListener {

    /**
     * 'Empty' mouseAdapter to set when startWaitCursor
     */
    private final static MouseAdapter mouseAdapter = new MouseAdapter() {
    };


    public EisDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);

        addKeyAndContainerListenerRecursively(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                performEscapeAction(null);
            }
        });

    }

    public EisDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        addKeyAndContainerListenerRecursively(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                performEscapeAction(null);
            }
        });

    }

    //The following function is recursive and is intended for internal use only.
    //It is private to prevent anyone calling it from other classes
    //The function takes a Component as an argument and adds this JDialog 
    //as a KeyListener to it.
    //Besides it checks if the component is actually a Container and if it is, 
    //there  are 2 additional things to be done to this Container :
    // 1 - add this Dialog as a ContainerListener to the Container
    // 2 - call this function recursively for every child of the Container.

    private void addKeyAndContainerListenerRecursively(Component c) {
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
                addKeyAndContainerListenerRecursively(children[i]);
            }
        }
    }

    //The following function is the same as the function above with the exception that it does exactly the opposite - removes this Dialog
    //from the listener lists of Components.

    private void removeKeyAndContainerListenerRecursively(Component c) {
        c.removeKeyListener(this);
        if (c instanceof Container) {
            Container cont = (Container) c;
            cont.removeContainerListener(this);
            Component[] children = cont.getComponents();
            for (int i = 0; i < children.length; i++) {
                removeKeyAndContainerListenerRecursively(children[i]);
            }
        }
    }

    //ContainerListener interface
    //---------------------------------------------------------

    //This function is called whenever a Component or a Container is added 
    //to another Container belonging to this Dialog

    public void componentAdded(ContainerEvent e) {
        addKeyAndContainerListenerRecursively(e.getChild());
    }

    //This function is called whenever a Component or a Container is removed from another Container belonging to this Dialog

    public void componentRemoved(ContainerEvent e) {
        removeKeyAndContainerListenerRecursively(e.getChild());
    }

    //KeyListener interface
    //---------------------------------------------------------

    //This function is called whenever a Component belonging to this Dialog 
    //(or the Dialog itself) gets the KEY_PRESSED event

    public void keyPressed(KeyEvent e) {
        if (e.isConsumed()) {
            return;
        } // Geert (2005-jul-01)
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            //Key pressed is the ESCAPE key.
            performEscapeAction(e);
        } else if (code == KeyEvent.VK_ENTER) {
            //Key pressed is the ENTER key. 
            //Redefine performEnterAction() in subclasses 
            //to respond to depressing the ENTER key.
            performEnterAction(e);
        }
        //Insert code to process other keys here
    }

    //We need the following 2 functions to complete imlementation of KeyListener

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void performEscapeAction(KeyEvent evt) {
        // In case this dialog is just used as a container for one (and only one)
        // JPanel [e.g. EisMain.showModalDialog()],
        // we try to 
        // 1. see if there is data to save
        // 2. the performEscapeAction() function on the panel.
        // If such a function is not available (or we're not in this specific case) 
        // we perform a simple dispose()
        Component[] children = getContentPane().getComponents();
        if (children.length != 1) {
            dispose();
            return;
        }

        Component innerComponent = children[0];
        // Special case: it is a DefaultPropsPnlContainer, so consider the props pnl within
        if (innerComponent instanceof DefaultPropsPnlContainer) {
            children = ((DefaultPropsPnlContainer) innerComponent).getComponents();
            if (children.length != 2) { // innerPnl + buttonPnl
                dispose();
                return;
            }
            innerComponent = children[0];
        }

        // 1) First check if the panel implements the DataEditingPnl interface
        if (innerComponent instanceof DataEditingPnl) {
            if (!discardChanges()) {
                return;
            }

            DataEditingPnl panel = (DataEditingPnl) innerComponent;
            panel.performEscapeAction();
        }
        // 2) Then try to call the panel's performEscapeAction() method
        else if (innerComponent instanceof JPanel) {
            if (!discardChanges()) {
                return;
            }

            JPanel panel = (JPanel) innerComponent;
            Method method = null;
            try {
                method = panel.getClass().getMethod("performEscapeAction", new Class[]{});
                if (method != null) {
                    try {
                        method.invoke(panel, (Object[]) null);
                        return;
                    } catch (Exception e) {
                        dispose();
                        return;
                    }
                }
            } catch (NoSuchMethodException e) {
                dispose();
                return;
            }
        } else {
            dispose();
        }
    }

    void performEnterAction(KeyEvent evt) {
        //Default response to ENTER key pressed goes here
        //Redefine this function in subclasses to respond to ENTER key differently
        Component[] children = getContentPane().getComponents();
        if (children.length != 1) {
            return;
        }
        Component innerComponent = children[0];
        if (!(innerComponent instanceof DataEditingPnl) &&
                !(innerComponent instanceof JPanel) &&
                !(innerComponent instanceof DefaultPropsPnlContainer)) {
            return;
        }

        if (evt.getSource() instanceof JComboBox) {
            JComboBox box = (JComboBox) evt.getSource();
            if (box.isPopupVisible()) {
                return;
            } // the <Enter> = making a choice in the combobox
        }

        // gde: dirty trick to avoid that an <Enter> in the (editable!) 
        //      speed combobox of a SerialCommunicationSettingsAspectEditor
        //      closes the whole dialog (better suggestions welcome):
        // side effect: an <Enter> in that combobox (even collapsed) never leads
        //              to a close of the dialog
        // System.out.println(">"+evt.getSource()+"<");
        if (evt.getSource().getClass().toString().equalsIgnoreCase(
                "class javax.swing.plaf.basic.BasicComboBoxEditor$BorderlessTextField")) {
            return;
        }

        // Special case: it is a DefaultPropsPnlContainer, so consider the props pnl within
        if (innerComponent instanceof DefaultPropsPnlContainer) {
            children = ((DefaultPropsPnlContainer) innerComponent).getComponents();
            if (children.length != 2) { // innerPnl + buttonPnl
                return;
            }
            innerComponent = children[0];
        }

        // 1) First check if the panel implements the DataEditingPnl interface
        if (innerComponent instanceof DataEditingPnl) {
            boolean goOn = ((DataEditingPnl) innerComponent).performEnterAction(evt);
            if (!goOn) {
                return;
            }
        } else {
            // 2) Try to trigger the panel's performEnterAction
            JPanel panel = (JPanel) innerComponent;
            Method method = null;
            try {
                // 1) with KeyEvent as parameter
                method = panel.getClass().getMethod("performEnterAction", new Class[]{KeyEvent.class});
                if (method != null) {
                    try {
                        Boolean goOn = (Boolean) method.invoke(panel, new Object[]{evt});
                        if (!goOn.booleanValue()) {
                            return;
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (NoSuchMethodException e) {
                try {
                    // 2) without parameters
                    method = panel.getClass().getMethod("performEnterAction", new Class[]{});
                    if (method != null) {
                        try {
                            method.invoke(panel, (Object[]) null);
                            return;
                        } catch (Exception ex) {
                        }
                    }
                } catch (NoSuchMethodException x) {
                }
            }
        }
    }

    // -------------------------------------------------------------

    private boolean discardChanges() {
        // In case this dialog is just used as a container for one (and only one)
        // JPanel [e.g. EisMain.showModalDialog()],
        // we try to call the isDataDirty() function on the panel.
        Component[] children = getContentPane().getComponents();
        if (children.length != 1) {
            return true;
        }

        Component innerComponent = children[0];
        // Special case: it is a DefaultPropsPnlContainer, so consider the props pnl within
        if (innerComponent instanceof DefaultPropsPnlContainer) {
            children = ((DefaultPropsPnlContainer) innerComponent).getComponents();
            if (children.length != 2) { // innerPnl + buttonPnl
                return true;
            }
            innerComponent = children[0];
        }

        // 1) First check if the panel implements the DataEditingPnl interface
        if (innerComponent instanceof DataEditingPnl) {
            DataEditingPnl panel = (DataEditingPnl) innerComponent;
            if (panel == null) {
                return true;
            }

            if (panel.isDataDirty()) {
                int choice =
                        JOptionPane.showConfirmDialog(getRootPane(),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("sureToDiscardChanges"),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("confirmation"),
                                JOptionPane.YES_NO_OPTION);
                return (choice == JOptionPane.YES_OPTION);
            } else {
                return true;
            } // nothing is dirty
        }
        // 2) Then try to call the isDataDirty() method by reflection 
        else if (innerComponent instanceof JPanel) {
            JPanel panel = (JPanel) innerComponent;
            if (panel == null) {
                return true;
            }

            Boolean bDirty = new Boolean(false);
            Method method = null;
            try {
                method = panel.getClass().getMethod("isDataDirty", new Class[]{});
                if (method != null) {
                    try {
                        bDirty = (Boolean) method.invoke(panel, (Object[]) null);
                    } catch (Exception e) {
                        return true;
                    }
                }
            } catch (NoSuchMethodException e) {
                return true;
            }
            if (bDirty.booleanValue()) {
                int choice =
                        JOptionPane.showConfirmDialog(getRootPane(),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("sureToDiscardChanges"),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("confirmation"),
                                JOptionPane.YES_NO_OPTION);
                return (choice == JOptionPane.YES_OPTION);
            } else {
                return true;
            } // nothing is dirty
        }
        return true;
    }

    public void startWaitCursor() {
        if (!getGlassPane().isVisible()) {
            getGlassPane().addMouseListener(mouseAdapter);
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            getGlassPane().setVisible(true);
        }
    }

    public void stopWaitCursor() {
        if (getGlassPane().isVisible()) {
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getGlassPane().removeMouseListener(mouseAdapter);
            getGlassPane().setVisible(false);
        }
    }

    public void addBackgroundImage(final ImageIcon bg_image) {
        final int winc = bg_image.getIconWidth();
        final int hinc = bg_image.getIconHeight();
        JLabel backlabel = new JLabel("");
        if (bg_image.getIconWidth() > 0 && bg_image.getIconHeight() > 0) {
            backlabel = new JLabel() {
                public void paintComponent(Graphics g) {
                    int w = getParent().getWidth();
                    int h = getParent().getHeight();
                    for (int i = 0; i < h + hinc; i = i + hinc) {
                        for (int j = 0; j < w + winc; j = j + winc) {
                            bg_image.paintIcon(this, g, j, i);
                        }
                    }
                }

                public Dimension getPreferredSize() {
                    return new Dimension(super.getSize());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        }
        ((JPanel) getContentPane()).setOpaque(false);
        getLayeredPane().add(backlabel, new Integer(Integer.MIN_VALUE));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = new Double(screen.getWidth()).intValue();
        int screenHeight = new Double(screen.getHeight()).intValue();
        backlabel.setBounds(0, 0, screenWidth, screenHeight);
    }

    protected JPanel createDefaultButtonPanel() {
        return new DefaultEisDialogButtonPanel();
    }

    protected JPanel createDefaultButtonPanel(String okButtonLabel) {
        return new DefaultEisDialogButtonPanel(okButtonLabel);
    }

    protected void okButtonActionPerformed() {
        // do nothing: to be implemented by subclasses
        // close the dialog
        closeDialog(null);
    }

    protected void cancelButtonActionPerformed() {
        // close the dialog
        closeDialog(null);
    }

    protected void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
    }

    protected class DefaultEisDialogButtonPanel extends JPanel {

        private JButton okButton;
        private JButton cancelButton;

        public DefaultEisDialogButtonPanel() {
            this(TranslatorProvider.instance.get().getTranslator().getTranslation("set"));
        }

        public DefaultEisDialogButtonPanel(String okButtonLabel) {
            initComponents(okButtonLabel);
        }

        public JButton getOkButton() {
            return okButton;
        }

        public JButton getCancelButton() {
            return cancelButton;
        }

        private void initComponents(String okButtonLabel) {
            setLayout(new FlowLayout(FlowLayout.RIGHT));

            JPanel buttonPanel = new JPanel();

            okButton = new JButton(okButtonLabel);
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    EisDialog.this.okButtonActionPerformed();
                }
            });

            cancelButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    EisDialog.this.cancelButtonActionPerformed();
                }
            });

            buttonPanel.setLayout(new GridLayout(1, 0, 6, 0));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            this.add(buttonPanel);

        }

    }
}