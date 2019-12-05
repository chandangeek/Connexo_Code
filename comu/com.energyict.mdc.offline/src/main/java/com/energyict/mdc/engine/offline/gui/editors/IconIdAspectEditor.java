package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.selecting.SelectIconPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AspectEditor that shows an icon according to the model's iconid
 */
public class IconIdAspectEditor extends AspectEditor<JPanel> {

    private Icon defaultIcon;

    private JLabel label;
    private JPanel iconPanel;
    private JLabel iconLabel;
    private JButton iconBrowseButton;
    private SelectIconPnl selectIconPanel;

    private String iconPath;

    public IconIdAspectEditor() {
        initComponents();
    }

    public void setDefaultIcon(Icon defaultIcon) {
        this.defaultIcon = defaultIcon;
        updateIconLabel();
    }

    public JLabel getLabelComponent() {
        return label;
    }

    protected void updateLabel() {
        label.setText(getLabelString());
    }

    public JPanel getValueComponent() {
        return iconPanel;
    }

    protected Object getViewValue() {
        return iconPath;
    }

    protected void setViewValue(Object value) {
        this.iconPath = (String) value;
        updateIconLabel();
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        iconBrowseButton.setVisible(!readOnly);
    }

    private void initComponents() {
        label = new JLabel();

        iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.LINE_AXIS));
        iconPanel.add(getIconLabel());
        iconPanel.add(Box.createRigidArea(new Dimension(4, 20)));
        iconPanel.add(getIconChooserButton());

    }

    private SelectIconPnl getSelectIconPanel() {
        if (selectIconPanel == null) {
            selectIconPanel = new SelectIconPnl();
        }
        return selectIconPanel;
    }

    private JLabel getIconLabel() {
        iconLabel = new JLabel();
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(22, 20));
        iconLabel.setMinimumSize(new Dimension(22, 20));
        iconLabel.setMaximumSize(new Dimension(22, 20));
        // Gives the iconLabel a border so the user sees there is a widget even when it's empty
        iconLabel.setBorder(new JTextField().getBorder());
        return iconLabel;
    }

    private JButton getIconChooserButton() {
        iconBrowseButton = new JButton("...");
        iconBrowseButton.setMargin(new Insets(0, 2, 0, 2));
        iconBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectIconPanel(); // initiate the selection panel if needed;
                selectIconPanel.setSelectedIconPath((String) getModelValue());
                UiHelper.getMainWindow().showModalDialog(selectIconPanel, TranslatorProvider.instance.get().getTranslator().getTranslation("selectIcon"));
                String iconPath = selectIconPanel.getSelectedIconPath();
                if (iconPath != null) {
                    setViewValue(iconPath);
                    updateModel();
                }
            }
        });
        return iconBrowseButton;
    }

    private void updateIconLabel() {
        iconLabel.setIcon(getIcon(iconPath));
    }

    private Icon getIcon(String iconPath) {
        if (iconPath == null) {
            return defaultIcon;
        }
        return MdwIcons.getCustomIcon(iconPath);
    }

}
