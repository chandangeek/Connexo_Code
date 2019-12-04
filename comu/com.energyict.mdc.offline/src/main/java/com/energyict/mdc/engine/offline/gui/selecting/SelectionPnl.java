package com.energyict.mdc.engine.offline.gui.selecting;

import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPanel allowing the user to select items out of a list
 *
 * @param <T> Type of objects to select
 */
public class SelectionPnl<T> extends JPanel {

    private ArrayList<T> selection = new ArrayList<T>();
    private List<T> itemsToShow; // when the check box is unselected
    private SelectAllAction selectAllAction;
    private ClearSelectionAction clearSelectionAction;
    private Options options;

    // Components
    private JPanel buttonPanel;
    private JPanel checkBoxPanel;
    private JButton noneButton;
    private JButton okButton;
    private JCheckBox theCheckBox;
    private JList theList;
    private JPanel southPnl;
    private JPanel extraInfoPnl;
    private JLabel extraInfoLbl;

    public SelectionPnl(List<T> itemsToShow) {
        this(itemsToShow, new Options());
    }

    public SelectionPnl(List<T> itemsToShow, boolean showNoneButton) {
        this(itemsToShow, new Options(showNoneButton));
    }

    public SelectionPnl(List<T> itemsToShow, Options options) {
        this.options = options;
        initComponents();
        okButton.setEnabled(false);
        setCheckBoxVisible(false); // by default
        if (!options.getShowNoneButton()) {
            buttonPanel.remove(noneButton);
        }
        setItems(itemsToShow);
        setPreferredSize(new Dimension(400, 330));

        setSelectionMode(options.getSelectionMode());
        setCellRenderer(options.getCellRenderer());
        if (options.getPreselectFirstItem() && itemsToShow.size() > 0) {
            theList.setSelectedIndex(0);
        }
    }

    public void setItems(List<T> itemsToShow){
         this.itemsToShow = itemsToShow;
         theList.setListData(itemsToShow.toArray());
    }

    public void setSelection(T obj) {
        theList.clearSelection();
        if (obj != null){
            if (this.itemsToShow.contains(obj)){
                theList.setSelectedValue(obj, true);
                theList.scrollRectToVisible(theList.getVisibleRect());
            }
        }
    }
    // -------------------------------------------------------------

    public JButton getDefaultButton() {
        return okButton;
    }
    // -------------------------------------------------------------

    private void setSelectionMode(int selectionMode) {
        theList.setSelectionMode(selectionMode);
        initActions();
    }

    // -------------------------------------------------------------

    public ArrayList<T> getSelection() {
        return selection;
    }

    // -------------------------------------------------------------

    public void setNoneButtonText(String text) {
        noneButton.setText(text);
    }

    // -------------------------------------------------------------

    public void setNoneButtonMnemonic(int value) {
        noneButton.setMnemonic(value);
    }

    public void setCheckBoxVisible(boolean visible) {
        if (options.isCheckBoxVisible() && !visible) {
            checkBoxPanel.remove(theCheckBox);
        } else if (!options.isCheckBoxVisible() && visible) {
            checkBoxPanel.add(theCheckBox);
        }
        options.setCheckBoxVisible(visible);
    }

    public void setCheckBoxText(String text) {
        setCheckBoxVisible(true);
        theCheckBox.setText(text);
    }

    public boolean isCheckBoxSelected() {
        return options.isCheckBoxVisible() && theCheckBox.isSelected();
    }

    private void setCellRenderer(ListCellRenderer renderer) {
        theList.setCellRenderer(renderer);
    }

    private void initActions() {
        if (selectAllAction == null) {
            selectAllAction = new SelectAllAction();
        }
        if (clearSelectionAction == null) {
            clearSelectionAction = new ClearSelectionAction();
            theList.addListSelectionListener(clearSelectionAction);
        }
        if (theList.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
            theList.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }
    }

    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem(selectAllAction));
        popupMenu.add(new JMenuItem(clearSelectionAction));
        return popupMenu;
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        this.add(getListPanel(), BorderLayout.CENTER);
        if (options.getExtraInfo() != null) {
            this.add(getSouthPnl(), BorderLayout.SOUTH);
        } else {
            this.add(getButtonPanel(), BorderLayout.SOUTH);
        }
    }

    private JPanel getListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new javax.swing.border.EmptyBorder(new Insets(3, 3, 3, 3)));

        theList = new JList();
        theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        theList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                okButton.setEnabled(theList.getSelectedIndex() != -1);
            }
        });
        theList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                theListMouseClicked(evt);
            }
        });

        listPanel.add(new JScrollPane(theList), BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel getButtonPanel() {

        theCheckBox = new JCheckBox();
        theCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object selectedObj = theList.getSelectedValue();
                if (theCheckBox.isSelected()) {
                    theList.setListData(options.getItemsToShowSelected().toArray());
                } else {
                    theList.setListData(itemsToShow.toArray());
                }
                theList.setSelectedValue(selectedObj, true);
            }
        });

        checkBoxPanel = new JPanel(new GridLayout(1, 0));
        checkBoxPanel.add(theCheckBox);

        noneButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("none"));
        noneButton.setMnemonic(KeyEvent.VK_N);
        noneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                noneButtonActionPerformed();
            }
        });

        okButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        JButton cancelButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 0, 6, 0));
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 4, 4, 2)));
        buttonPanel.add(noneButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(checkBoxPanel, BorderLayout.WEST);
        lowerPanel.add(buttonPanel, BorderLayout.EAST);
        return lowerPanel;
    }

    private void theListMouseClicked(MouseEvent evt) {
        if ((theList.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
                && SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
            okButtonActionPerformed();
        }
    }


    // If the selection list contains the 'null' item 
    // this means <None> has been pressed
    private void noneButtonActionPerformed() {
        selection.clear();
        selection.add(null);
        doClose();
    }

    // An empty selection list means <Cancel> has been pressed

    private void cancelButtonActionPerformed() {
        selection.clear();
        doClose();
    }

    @SuppressWarnings("unchecked")
    private void okButtonActionPerformed() {
        selection.clear();
        if (theList.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
            if (theList.getSelectedValue() != null) {
                selection.add((T) theList.getSelectedValue());
            } else {
                return;
            }
        } else if (theList.getSelectionMode() ==
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            Object selectedValues[] = theList.getSelectedValues();
            for (int i = 0; i < selectedValues.length; i++) {
                selection.add((T) selectedValues[i]);
            }
        }
        doClose();
    }

    // -------------------------------------------------------------

    private void doClose() {
        JDialog parentDialog = (JDialog) (getRootPane().getParent());
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }

    private class SelectAllAction extends AbstractAction {

        public SelectAllAction() {
            putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("selectAll"));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));  // Internationalization?!!!
        }

        public void actionPerformed(ActionEvent e) {
            theList.setSelectionInterval(0, theList.getModel().getSize() - 1);
        }
    }

    private JPanel getSouthPnl() {
        if (southPnl == null) {
            southPnl = new JPanel(new BorderLayout());
            JPanel borderPnl = new JPanel(new BorderLayout());
            borderPnl.setBorder(new CompoundBorder(
                    BorderFactory.createEmptyBorder(0, 3, 3, 3), BorderFactory.createLineBorder(Color.GRAY)));
            JLabel iconLbl = new JLabel(MdwIcons.INFORMATION_ICON);
            iconLbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            borderPnl.add(iconLbl, BorderLayout.WEST);
            borderPnl.add(getExtraInfoPnl(), BorderLayout.CENTER);
            southPnl.add(borderPnl, BorderLayout.NORTH);
            southPnl.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return southPnl;
    }

    private JPanel getExtraInfoPnl() {
        if (extraInfoPnl == null) {
            extraInfoPnl = new JPanel(new BorderLayout());
            extraInfoPnl.add(getExtraInfoLbl(), BorderLayout.CENTER);
        }
        return extraInfoPnl;
    }

    private JLabel getExtraInfoLbl() {
        if (extraInfoLbl == null) {
            extraInfoLbl = new JLabel(options.getExtraInfo());
            extraInfoLbl.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 15));
        }
        return extraInfoLbl;
    }

    private class ClearSelectionAction extends AbstractAction
            implements javax.swing.event.ListSelectionListener {

        public ClearSelectionAction() {
            putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("clearSelection"));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));  // Internationalization?!!!
        }

        public void actionPerformed(ActionEvent e) {
            theList.clearSelection();
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
            this.enabled = (theList.getSelectedIndex() != -1);
        }

    }

    public static class Options<T> {

        private String extraInfo;
        private int selectionMode = ListSelectionModel.SINGLE_SELECTION;
        private boolean showNoneButton = true;
        private boolean preselectFirstItem = true;
        private boolean checkBoxVisible = true;
        private List<T> itemsToShowSelected = new ArrayList<T>();
        private ListCellRenderer renderer = new BasicComboBoxRenderer();

        public Options() {
        }

        public Options(boolean showNoneButton) {
            this.showNoneButton = showNoneButton;
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public void setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
        }

        public boolean getShowNoneButton() {
            return showNoneButton;
        }

        public void setShowNoneButton(boolean showNoneButton) {
            this.showNoneButton = showNoneButton;
        }

        public boolean getPreselectFirstItem() {
            return preselectFirstItem;
        }

        public void setPreselectFirstItem(boolean preselectFirstItem) {
            this.preselectFirstItem = preselectFirstItem;
        }

        public List<T> getItemsToShowSelected() {
            return itemsToShowSelected;
        }

        public void setItemsToShowSelected(List<T> itemsToShowSelected) {
            this.itemsToShowSelected = itemsToShowSelected;
        }

        public boolean isCheckBoxVisible() {
            return checkBoxVisible;
        }

        public void setCheckBoxVisible(boolean checkBoxVisible) {
            this.checkBoxVisible = checkBoxVisible;
        }

        public int getSelectionMode() {
            return selectionMode;
        }

        public void setSelectionMode(int selectionMode) {
            this.selectionMode = selectionMode;
        }

        public ListCellRenderer getCellRenderer() {
            return renderer;
        }

        public void setCellRenderer(ListCellRenderer renderer) {
            this.renderer = renderer;
        }
    }
}