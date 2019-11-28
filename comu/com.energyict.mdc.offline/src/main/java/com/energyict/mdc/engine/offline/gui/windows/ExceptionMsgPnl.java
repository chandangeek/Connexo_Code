package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.core.exception.BusinessException;
import com.energyict.mdc.engine.offline.core.exception.HtmlEnabledBusinessException;
import com.energyict.mdc.engine.offline.gui.dialogs.ExceptionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class ExceptionMsgPnl extends EisPropsPnl {

    protected JPanel buttonPanel;
    protected JButton cancelButton;
    protected JButton detailsButton;
    protected JButton okContinueButton;
    protected JPanel southPanel;

    private JPanel msgPanel;
    private JPanel htmlMsgPnl;
    private JLabel iconLabel;
    private JLabel msgLabel;

    protected Throwable exception = null;
    protected Icon icon = null;
    protected String message = null;
    private String errorCode = null;
    protected boolean cancelPossible = false;
    private boolean cancelPressed = false;

    /**
     * Creates new form MsgPnl
     */
    public ExceptionMsgPnl(String message, Throwable ex) {
        this(message, ex, false);
    }

    public ExceptionMsgPnl(String message, Throwable ex, boolean cancelPossible) {
        this(message, ex, false, UIManager.getIcon("OptionPane.errorIcon"));
    }

    public ExceptionMsgPnl(String message, Throwable ex, boolean cancelPossible, Icon icon) {
        this.exception = ex;
        if (ex instanceof HtmlEnabledBusinessException){
            this.message =((HtmlEnabledBusinessException)ex).getHtmlMessage();
        } else{
            if (message.indexOf("<br>")>0){
                this.message = "<html>"+message+"</html>";
            } else {
                this.message = message;
            }
        }
        if (ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            this.errorCode = businessException.getErrorCode();
        }
        this.icon = icon;
        this.cancelPossible = cancelPossible;
        initButtonPanel();
        if (ex instanceof HtmlEnabledBusinessException) {
            initMultipleMessagePanel();
        } else {
            initMessagePanel();
        }
        if (cancelPossible) {
            okContinueButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("continue"));
            okContinueButton.setMnemonic(KeyEvent.VK_C);
        } else {
            okContinueButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
            buttonPanel.remove(cancelButton);
        }

        detailsButton.setFocusable(false);
    }

    public boolean userWantsToCancel() {
        return cancelPressed;
    }

    protected void initButtonPanel() {
        southPanel = new JPanel();
        buttonPanel = new JPanel();
        detailsButton = new JButton();
        detailsButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("details"));
        detailsButton.setMnemonic(KeyEvent.VK_D);
        okContinueButton = new JButton();
        cancelButton = new JButton();

        setLayout(new BorderLayout());

        southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        southPanel.setMaximumSize(new Dimension(32767, 35));
        buttonPanel.setLayout(new GridLayout(1, 0, 6, 0));

        detailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                detailsButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(detailsButton);

        okContinueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okContinueButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okContinueButton);

        cancelButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        southPanel.add(buttonPanel);

        add(southPanel, BorderLayout.SOUTH);

    }

    protected void initMessagePanel() {
        msgPanel = new JPanel();

        iconLabel = new JLabel(icon);
        msgLabel = new JLabel(message);

        msgPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        msgPanel.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(5, 5, 5, 5);
        msgPanel.add(iconLabel, gc);

        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        msgPanel.add(msgLabel, gc);

        if (!Utils.isNull(errorCode)) {
            gc = new GridBagConstraints();
            gc.gridx = 1;
            gc.gridy = 1;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(5, 5, 5, 5);

            JLabel errorCodeLabel = new JLabel("(" + TranslatorProvider.instance.get().getTranslator().getTranslation("errorCode") + ": " + errorCode + ")");
            msgPanel.add(errorCodeLabel, gc);
        }
        JScrollPane scrollPane = new JScrollPane(msgPanel);
        scrollPane.setMaximumSize(new Dimension(900, 500));
        add(scrollPane, BorderLayout.CENTER);
    }

    protected void initMultipleMessagePanel() {
        msgPanel = new JPanel();
        msgPanel.setLayout(new BorderLayout());
        msgPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel iconPanel = new JPanel();
        iconPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        iconPanel.setLayout(new BorderLayout());
        iconLabel = new JLabel(icon);
        iconPanel.add(iconLabel, BorderLayout.NORTH);

        htmlMsgPnl = new JPanel();
        JScrollPane scrollPane = new JScrollPane();
        JTextPane msgTextPane = new JTextPane();
        msgTextPane.setEditable(false);
        msgTextPane.setContentType("text/html");
        HTMLEditorKit editorKit = (HTMLEditorKit) msgTextPane.getEditorKit();
        StyleSheet styles = editorKit.getStyleSheet();
        StringBuffer rule = new StringBuffer("body { font-family:");
        rule.append(iconLabel.getFont().getFamily());
        rule.append(",arial; ");
        rule.append("font-size:");
        rule.append(iconLabel.getFont().getSize());
        rule.append("}");
        styles.addRule(rule.toString());

        msgTextPane.setPreferredSize(new Dimension(450, 250));
        msgTextPane.setText(message);

        htmlMsgPnl.setLayout(new BorderLayout(5, 5));
        htmlMsgPnl.setPreferredSize(new Dimension(450, 250));
        scrollPane.setPreferredSize(new Dimension(450, 250));
        scrollPane.setRequestFocusEnabled(false);
        scrollPane.setViewportView(msgTextPane);

        htmlMsgPnl.add(scrollPane, BorderLayout.CENTER);

        msgPanel.add(iconPanel, BorderLayout.WEST);
        msgPanel.add(htmlMsgPnl, BorderLayout.CENTER);
        add(msgPanel, BorderLayout.CENTER);
    }

    public void performEscapeAction() {
        // Mantis issue #4075
        // only set cancelPressed = true if cancelPossible is true
        cancelPressed = cancelPossible ? true : false;
        doClose();
    }

    private void okContinueButtonActionPerformed(ActionEvent evt) {
        cancelPressed = false;
        doClose();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        performEscapeAction();
    }

    private void detailsButtonActionPerformed(ActionEvent evt) {
        new ExceptionDialog().handle(exception);
    }
}