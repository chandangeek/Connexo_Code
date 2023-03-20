package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.offline.core.OfflinePropertiesProvider;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;

import com.jidesoft.swing.JideSwingUtilities;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

public class AboutBoxPnl extends JPanel {
    private javax.swing.JLabel buildLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel copyrightLabel;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JLabel protocolTesterLabel;
    private javax.swing.JLabel faxLabel;
    private javax.swing.JLabel faxLabel2;
    private javax.swing.JLabel homepageLabel;
    private javax.swing.JLabel homepageLabel2;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel lowerPanel;
    private javax.swing.JLabel mailLabel;
    private javax.swing.JLabel mailLabel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel pictureLabel;
    private javax.swing.JLabel telephoneLabel;
    private javax.swing.JLabel telephoneLabel2;
    private javax.swing.JScrollPane theScrollPane;
    private javax.swing.JTabbedPane theTabbedPane;
    private javax.swing.JPanel versionPanel;
    private JLabel contactLabel;
    private JLabel supportLabel;
    private JLabel supportEmailLabel;
    private JLabel supportEmailField;

    private boolean firstTime = true;
    private TableBubbleSortDecorator tableDecorator = null;

    public AboutBoxPnl() {
        this("ComServer Mobile");
    }

    public AboutBoxPnl(String applicationName) {
        initComponents();
        pictureLabel.setIcon(EisIcons.ENERGYICT_ICON);
        String version = OfflinePropertiesProvider.getInstance().getConnexoVersion();
        version = version == null ? "" : version + " ";
        protocolTesterLabel.setText(applicationName + " " + version);
        buildLabel.setText("(Build " + version + ")");

        initDetailsPanel();
    }

    private void initDetailsPanel() {
        String[] items = {
            "servletURL",
            "java.version", "java.home",
            "os.name", "os.arch", "os.version",
            "timeZone", // i==6
            "user.name", "user.home", "user.dir",
            "java.io.tmpdir", "java.ext.dirs",
            "java.class.path", "java.library.path",
        };

        StringBuffer htmlText = new StringBuffer();
        int fontSize = Math.max(1, UiHelper.getFontSize() / 4);
        for (int i = 0; i < items.length; i++) {
            String tmp = TranslatorProvider.instance.get().getTranslator().getTranslation(items[i]) + ":";
            htmlText.append("<font face=\"verdana,arial,helvetica\" size=" + fontSize + ">");
            htmlText.append("&#8226;&nbsp;<B>" + tmp + "</B><BR>");
            switch(i) {
                case 0:
                    tmp = new RemoteProperties(OfflineComServerProperties.getInstance().getProperties()).getRemoteQueryApiUrl();
                    break;
                case 6:
                    tmp = java.util.TimeZone.getDefault().getID();
                    break;
                default:
                    tmp = System.getProperty(items[i]);
                    break;
            }
            htmlText.append("<font face=\"verdana,arial,helvetica\" size=" + fontSize + ">");
            htmlText.append(tmp + "<BR>");
        }
        editorPane.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        editorPane.setText(htmlText.toString());
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        pictureLabel = new javax.swing.JLabel();
        theTabbedPane = new javax.swing.JTabbedPane();
        contentPanel = new javax.swing.JPanel();
        versionPanel = new javax.swing.JPanel();
        protocolTesterLabel = new JLabel();
        buildLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        infoPanel = new javax.swing.JPanel();
        copyrightLabel = new javax.swing.JLabel();
        telephoneLabel = new javax.swing.JLabel();
        telephoneLabel2 = new javax.swing.JLabel();
        faxLabel = new javax.swing.JLabel();
        faxLabel2 = new javax.swing.JLabel();
        mailLabel = new javax.swing.JLabel();
        mailLabel2 = new javax.swing.JLabel();
        homepageLabel = new javax.swing.JLabel();
        homepageLabel2 = new javax.swing.JLabel();
        detailsPanel = new javax.swing.JPanel();
        theScrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        lowerPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        closeButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("close"));
        closeButton.setMnemonic(KeyEvent.VK_C);
        contactLabel = new JLabel();
        mailLabel = new javax.swing.JLabel();
        supportLabel = new javax.swing.JLabel();
        supportEmailLabel = new javax.swing.JLabel();
        supportEmailField = new javax.swing.JLabel();

        setLayout(new BorderLayout());

        theTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                theTabbedPaneStateChanged(evt);
            }
        });

        contentPanel.setLayout(new BorderLayout());

        versionPanel.setLayout(new GridBagLayout());

        protocolTesterLabel.setFont(new Font("Verdana", 1, 18));
        protocolTesterLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        versionPanel.add(protocolTesterLabel, gridBagConstraints);

        buildLabel.setFont(new Font("Dialog", 0, 12));
        buildLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        versionPanel.add(buildLabel, gridBagConstraints);

        contentPanel.add(versionPanel, BorderLayout.CENTER);

        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        infoPanel.setLayout(new GridBagLayout());

        copyrightLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        copyrightLabel.setText("Copyright " + "\u00A9" + " Honeywell International Inc. All rights reserved");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(copyrightLabel, gridBagConstraints);

        contactLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        Map<TextAttribute, Object> map = new Hashtable();
        map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        contactLabel.setFont(contactLabel.getFont().deriveFont(map));
        contactLabel.setText("Contact:");
        contactLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(10, 2, 2, 2);
        infoPanel.add(contactLabel, gridBagConstraints);

        telephoneLabel.setFont(new Font("Dialog", 0, 12));
        telephoneLabel.setText("Telephone: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(telephoneLabel, gridBagConstraints);

        telephoneLabel2.setFont(new Font("Dialog", 0, 12));
        telephoneLabel2.setText("+32 56 245 690");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(telephoneLabel2, gridBagConstraints);

        faxLabel.setFont(new Font("Dialog", 0, 12));
        faxLabel.setText("Fax: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(faxLabel, gridBagConstraints);

        faxLabel2.setFont(new Font("Dialog", 0, 12));
        faxLabel2.setText("+32 56 245 699");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(faxLabel2, gridBagConstraints);

        mailLabel.setFont(new Font("Dialog", 0, 12));
        mailLabel.setText("Email:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(mailLabel, gridBagConstraints);

        mailLabel2.setFont(new Font("Dialog", 0, 12));
        mailLabel2.setText("Info.EnergyICT@elster.com");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(mailLabel2, gridBagConstraints);

        homepageLabel.setFont(new Font("Dialog", 0, 12));
        homepageLabel.setText("Web:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(homepageLabel, gridBagConstraints);

        homepageLabel2.setFont(new Font("Dialog", 0, 12));
        homepageLabel2.setText("www.elstersolutions.com");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(homepageLabel2, gridBagConstraints);

        supportLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        supportLabel.setFont(supportLabel.getFont().deriveFont(map));
        supportLabel.setText("Support:");
        supportLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(supportLabel, gridBagConstraints);

        supportEmailLabel.setFont(new Font("Dialog", 0, 12));
        supportEmailLabel.setText("Email:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(supportEmailLabel, gridBagConstraints);

        supportEmailField.setFont(new Font("Dialog", 0, 12));
        supportEmailField.setText("Support.EnergyICT@elster.com");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        infoPanel.add(supportEmailField, gridBagConstraints);

        mainPanel.add(infoPanel);

        contentPanel.add(mainPanel, BorderLayout.SOUTH);

        theTabbedPane.addTab(TranslatorProvider.instance.get().getTranslator().getTranslation("about"), contentPanel);

        detailsPanel.setLayout(new BorderLayout(0, 0));

        detailsPanel.setBorder(new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)));
        theScrollPane.setPreferredSize(new Dimension(500, 350));
        editorPane.setPreferredSize(new Dimension(500, 350));
        theScrollPane.setViewportView(editorPane);

        detailsPanel.add(theScrollPane, BorderLayout.CENTER);

        theTabbedPane.addTab(TranslatorProvider.instance.get().getTranslator().getTranslation("details"), detailsPanel);

        add(theTabbedPane, BorderLayout.CENTER);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        lowerPanel.setLayout(new BorderLayout(0, 0));

        JPanel logoPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pictureLabel.setBorder(new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)));
        logoPnl.add(pictureLabel);
        lowerPanel.add(logoPnl, BorderLayout.WEST);

        JPanel btnPnl = new JPanel(new BorderLayout(0, 0));
        btnPnl.setBorder(new javax.swing.border.EmptyBorder(new Insets(5, 5, 3, 3)));
        btnPnl.add(closeButton, BorderLayout.SOUTH);
        lowerPanel.add(btnPnl, BorderLayout.EAST);

        add(lowerPanel, BorderLayout.SOUTH);

        JideSwingUtilities.setOpaqueRecursively(this, true);
        setBackground(Color.WHITE);
        logoPnl.setBackground(Color.WHITE);
        pictureLabel.setBackground(Color.WHITE);
        lowerPanel.setBackground(Color.WHITE);
        btnPnl.setBackground(Color.WHITE);
        JideSwingUtilities.setOpaqueRecursively(theTabbedPane, false);
    }

    private void theTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {
        if (theTabbedPane.getSelectedIndex() != 0 && firstTime) {
            firstTime = false;
            theScrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        JDialog parentDialog = (JDialog) (getRootPane().getParent());
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }
}
