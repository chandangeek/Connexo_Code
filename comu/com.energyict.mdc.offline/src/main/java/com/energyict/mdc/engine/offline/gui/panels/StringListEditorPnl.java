package com.energyict.mdc.engine.offline.gui.panels;

import com.elster.jupiter.time.RelativeDate;
import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.core.EscDialog;
import com.energyict.mdc.engine.offline.gui.dialogs.ExceptionDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: gde
 * Date: 7/03/13
 * JPanel used in the StringListAspectEditor
 * Lets you add, remove, edit and order the strings in the List
 */

public class StringListEditorPnl extends JPanel {

    private final static Icon UP_ICON = new ImageIcon(RelativeDate.class.getResource("/images/up.gif"));
    private final static Icon DOWN_ICON = new ImageIcon(RelativeDate.class.getResource("/images/down.gif"));

    private JPanel centerPnl;
    private JList theList;
    private JPanel eastButtonPnl;
    private JButton addBtn;
    private JButton editBtn;
    private JButton upBtn;
    private JButton downBtn;
    private JButton deleteBtn;
    private JPanel southButtonPnl;
    private JButton okBtn;
    private JButton cancelBtn;

    private boolean readOnly = false;
    private boolean canceled;
    private List<String> stringList;

    public StringListEditorPnl(List<String> stringList) {
        this.stringList = new ArrayList<>(stringList);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(getCenterPnl(), BorderLayout.CENTER);
        add(getSouthButtonPnl(), BorderLayout.SOUTH);
    }

    public List<String> getStringList() {
        return stringList;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setReadOnly(boolean flag) {
        this.readOnly = flag;
        if (this.readOnly) {
            getCenterPnl().remove(getEastButtonPnl());
            getOkBtn().setVisible(false);
            getCancelBtn().setText(TranslatorProvider.instance.get().getTranslator().getTranslation("close"));
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 350);
    }

    private JPanel getCenterPnl() {
        if (centerPnl==null) {
            centerPnl = new JPanel(new BorderLayout());
            centerPnl.add(new JScrollPane(getList()), BorderLayout.CENTER);
            centerPnl.add(getEastButtonPnl(), BorderLayout.EAST);
        }
        return centerPnl;
    }

    private JPanel getSouthButtonPnl() {
        if (southButtonPnl==null) {
            JPanel innerPnl = new JPanel(new GridLayout(1, 0, 5, 0));
            innerPnl.add(getOkBtn());
            innerPnl.add(getCancelBtn());
            innerPnl.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

            southButtonPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
            southButtonPnl.add(innerPnl);
        }
        return southButtonPnl;
    }

    private JButton getOkBtn() {
        if (okBtn==null) {
            okBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    canceled = false;
                    doClose();
                }
            });
        }
        return okBtn;
    }

    private JButton getCancelBtn() {
        if (cancelBtn==null) {
            cancelBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    canceled = true;
                    doClose();
                }
            });
        }
        return cancelBtn;
    }

    private JPanel getEastButtonPnl() {
        if (eastButtonPnl==null) {
            JPanel innerPnl = new JPanel(new GridLayout(0, 1, 3, 3));
            innerPnl.add(getAddBtn());
            innerPnl.add(getEditBtn());
            innerPnl.add(getUpBtn());
            innerPnl.add(getDownBtn());
            innerPnl.add(getDeleteBtn());
            innerPnl.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

            eastButtonPnl = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
            eastButtonPnl.add(innerPnl);
        }
        return eastButtonPnl;
    }

    private JButton getAddBtn() {
        if (addBtn==null) {
            addBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("add"));
            addBtn.setMnemonic(KeyEvent.VK_A);
            addBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stringList.add(stringList.size(), "");
                    editString(TranslatorProvider.instance.get().getTranslator().getTranslation("add"), stringList.size()-1, true);
                    refreshList();
                }
            });
        }
        return addBtn;
    }

    private JButton getEditBtn() {
        if (editBtn==null) {
            editBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("edit"));
            editBtn.setMnemonic(KeyEvent.VK_E);
            editBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    editString(TranslatorProvider.instance.get().getTranslator().getTranslation("edit"), getList().getSelectionModel().getMinSelectionIndex(), false);
                    refreshList();
                }
            });
        }
        return editBtn;
    }

    private JButton getUpBtn() {
        if (upBtn==null) {
            upBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("up"));
            upBtn.setIcon(UP_ICON);
            upBtn.setMnemonic(KeyEvent.VK_U);
            upBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ListSelectionModel lsm = getList().getSelectionModel();
                    int min = lsm.getMinSelectionIndex();
                    int max = lsm.getMaxSelectionIndex();
                    String itemToGoDown = stringList.get(min-1);
                    stringList.remove(min-1);
                    stringList.add(max, itemToGoDown);
                    refreshList();
                    getList().getSelectionModel().addSelectionInterval(min-1, max-1);
                }
            });
        }
        return upBtn;
    }

    private JButton getDownBtn() {
        if (downBtn==null) {
            downBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("down"));
            downBtn.setIcon(DOWN_ICON);
            downBtn.setMnemonic(KeyEvent.VK_O);
            downBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ListSelectionModel lsm = getList().getSelectionModel();
                    int min = lsm.getMinSelectionIndex();
                    int max = lsm.getMaxSelectionIndex();
                    String itemToGoUp = stringList.get(max+1);
                    stringList.remove(max+1);
                    stringList.add(min, itemToGoUp);
                    refreshList();
                    getList().getSelectionModel().addSelectionInterval(min+1, max+1);
                }
            });
        }
        return downBtn;
    }

    private JButton getDeleteBtn() {
        if (deleteBtn==null) {
            deleteBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("delete"));
            deleteBtn.setMnemonic(KeyEvent.VK_D);
            deleteBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ListSelectionModel lsm = getList().getSelectionModel();
                    int min = lsm.getMinSelectionIndex();
                    int max = lsm.getMaxSelectionIndex();
                    for (int i=max; i>=min; i--) {
                        stringList.remove(i);
                    }
                    refreshList();
                }
            });
        }
        return deleteBtn;
    }

    private JList getList() {
        if (theList==null) {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String each : stringList) {
                model.addElement(each);
            }
            theList = new JList(model);
            theList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            theList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    updateButtons();
                }
            });
            updateButtons();
            theList.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) { onDoubleClick(e); }
                @Override public void mousePressed(MouseEvent e) { onDoubleClick(e); }
                private void onDoubleClick(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
                        editBtn.doClick();
                    }
                }
            });
        }
        return theList;
    }

    private void updateButtons() {
        ListSelectionModel lsm = getList().getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        getEditBtn().setEnabled(min!=-1 && min==max);
        getUpBtn().setEnabled(min!=-1 && min>0);
        getDownBtn().setEnabled(min!=-1 && max<stringList.size()-1);
        getDeleteBtn().setEnabled(min!=-1);
    }

    private void refreshList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String each : stringList) {
            model.addElement(each);
        }
        getList().setModel(model);
    }

    private void doClose() {
        if (getRootPane().getParent() instanceof JDialog) {
            JDialog parentDialog = (JDialog) (getRootPane().getParent());
            parentDialog.setVisible(false);
            parentDialog.dispose();
        }
    }

    private void editString(String title, final int index, final boolean addMode) {
        JFrame parentFrame = (JFrame)(UserEnvironment.getDefault().get(ExceptionDialog.PARENTFRAME));
        final StringPnl pnl = new StringPnl(stringList.get(index));
        EscDialog dlg = new EscDialog(parentFrame == null ? new JFrame() : parentFrame, true) {
            @Override
            public void performEscapeAction(KeyEvent evt) {
                pnl.performEscapeAction();
            }
        };
        dlg.setTitle(title);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(pnl, BorderLayout.CENTER);
        UiHelper.setDefaultButtonInDialog(dlg, pnl);
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        if (pnl.isCanceled()) {
            if (addMode) {
                stringList.remove(index);
            }
            return;
        }
        stringList.remove(index);
        stringList.add(index, pnl.getTheString());
    }

    private class StringPnl extends JPanel implements KeyListener {

        private String theString;
        private JTextField textField = new JTextField(30);
        private JButton okBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        private JButton cancelBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        private boolean canceled;

        public StringPnl(String initialString) {
            theString = initialString;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            textField.setText(theString);
            textField.addKeyListener(this);
            add(textField, BorderLayout.NORTH);
            JPanel btnPnl = new JPanel(new GridLayout(1,0,3,3));
            btnPnl.add(okBtn);
            btnPnl.add(cancelBtn);
            JPanel keepRightPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
            keepRightPnl.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
            keepRightPnl.add(btnPnl);
            add(keepRightPnl, BorderLayout.SOUTH);
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okBtnPressed();
                }
            });
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performEscapeAction();
                }
            });
        }

        private void okBtnPressed() {
            canceled = false;
            doClose();
        }

        public void performEscapeAction() {
            canceled = true;
            doClose();
        }

        public boolean isCanceled() {
            return canceled;
        }

        public String getTheString() {
            return textField.getText();
        }

        private void doClose() {
            if (getRootPane().getParent() instanceof JDialog) {
                JDialog parentDialog = (JDialog) (getRootPane().getParent());
                parentDialog.setVisible(false);
                parentDialog.dispose();
            }
        }

        // KeyListener interface
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_ENTER) {
                okBtnPressed();
            }
        }
        public void keyReleased(KeyEvent e) {}

    }
}
