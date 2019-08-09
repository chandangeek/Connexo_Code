/*
 * ExceptionDialog.java
 *
 * Created on 6 februari 2003, 16:46
 */
package com.energyict.mdc.engine.offline.gui.dialogs;

import com.energyict.mdc.engine.offline.DefaultFormatProvider;
import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.core.exception.*;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

public class ExceptionDialog extends JDialog implements Thread.UncaughtExceptionHandler {

    private static Icon UP_ICON;
    private static Icon DOWN_ICON;
    static {
        UP_ICON = new ImageIcon(ExceptionDialog.class.getResource("/images/up.gif"));
        DOWN_ICON = new ImageIcon(ExceptionDialog.class.getResource("/images/down.gif"));
    }

    // Components
    private JLabel errorCodeLabel;
    private JTextArea messageField;
    private JTextPane htmlMessageField;
    private JTextArea stackTraceField;
    private JPanel stackTracePanel;
    private JButton detailsButton;
    private Dimension previousSize;

    Throwable exception;

    private boolean unhandled = false;

    public static final String PARENTFRAME = "parentFrame";
    public static final String VERSIONINFO = "versionInfo";

    /**
     * Creates new form ExceptionDialog
     */
    public ExceptionDialog(Frame parent, boolean modal, Throwable ex) {
        this(parent, modal);
        setTitle(ex.toString());
        this.exception = ex;
        initComponents();
        updateComponents();
    }

    public ExceptionDialog() {
        this((Frame) UserEnvironment.getDefault().get(PARENTFRAME), true);
    }

    private ExceptionDialog(Frame owner, boolean modal) {
        super(owner, modal);
        setSize(getDefaultSize());
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
    }

    public Dimension getDefaultSize(){
        Dimension dFullScreen = this.getToolkit().getScreenSize();
        return  new Dimension(dFullScreen.width * 75 / 100, dFullScreen.height * 80 / 100);
    }

    //Thread.UncaughtExceptionHandler implementation
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        unhandled = true;
        handle(e);
    }

    // entry for all unhandled exceptions:
    public void handle(Throwable throwable) {
        handleIt(throwable);
    }

    private String getExceptionMessage(Throwable ex) {
        String message = null;
        if (ex instanceof HtmlEnabledBusinessException) {
            message = ((HtmlEnabledBusinessException)ex).getHtmlMessage();
        }
        if ( Utils.isNull(message) ) {
            message = ex.getLocalizedMessage();
            if ( Utils.isNull(message) ) {
                message = ex.getMessage();
                if ( Utils.isNull(message) ) {
                    message = ex.toString();
                }
            }
        }
        return message;
    }

    private void updateComponents(){
        String versionInfo = (String)UserEnvironment.getDefault().get(VERSIONINFO);

        errorCodeLabel.setText(versionInfo);
        if (this.exception instanceof BusinessException) {
            BusinessException be = (BusinessException) this.exception;
            errorCodeLabel.setText(versionInfo + " - " +
                TranslatorProvider.instance.get().getTranslator().getTranslation("errorCode")+ ": " + be.getErrorCode());
        }

        if (this.exception instanceof HtmlEnabledBusinessException) {
            htmlMessageField.setText(getExceptionMessage(this.exception));
            htmlMessageField.setCaretPosition(0);
        } else {
            messageField.setText(getExceptionMessage(this.exception));
            messageField.setCaretPosition(0);
        }

        StringWriter stringWriter = new StringWriter();
        this.exception.printStackTrace(new PrintWriter(stringWriter));

        stackTraceField.setText(stringWriter.toString());

        if (this.exception instanceof MultipleErrorMessagesException) {
            StringBuilder stringBuilder = new StringBuilder(stackTraceField.getText());
            for (Throwable exception : this.exception.getSuppressed()) {
                stringBuilder.append(System.getProperty("line.separator"));
                stringBuilder.append(System.getProperty("line.separator"));
                if (exception instanceof CompositeCommandBusinessException) {
                    stringBuilder.append(((CompositeCommandBusinessException)exception).getCompositeCommandName()+" - ");
                }
                if (exception instanceof BusinessException) {
                    stringBuilder.append(((BusinessException)exception).getErrorCode()+": ");
                } else {
                    stringBuilder.append( exception.getClass().getSimpleName() + ": ");
                }
                stringBuilder.append(System.getProperty("line.separator"));
                stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                stringBuilder.append(stringWriter.toString());
            }
            stackTraceField.setText(stringBuilder.toString());
        }
        stackTraceField.setCaretPosition(0);

        detailsButton.setIcon(stackTracePanel.isVisible() ? UP_ICON : DOWN_ICON);
    }

    private void handleIt(Throwable throwable) {
        try {
            handleDataBaseExceptions(throwable);

            this.exception = throwable;

            initTitle();
            initComponents();
            updateComponents();

            setPreferredSize(getDefaultSize());
            invalidate();
            pack();
            setLocationRelativeTo(null); // center
            setVisible(true);
            toFront();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.flush();
            System.exit(2);
        }
    }


    private void handleDataBaseExceptions(Throwable throwable) {
        if (throwable instanceof SQLException || throwable instanceof DatabaseException) {
            //TODO close the db connection
//            FormatProvider.instance.get().closeConnection();
        }
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new JideBoxLayout(container, BoxLayout.Y_AXIS));
        container.add(getMessageComponent(), JideBoxLayout.FIX);
        container.add(getStackTraceComponent(), JideBoxLayout.VARY);
        container.add(getButtonComponent(), JideBoxLayout.FLEXIBLE);

        this.setContentPane(container);
    }

    private void initTitle(){
       this.setTitle(TranslatorProvider.instance.get().getTranslator().getTranslation(unhandled ? "unhandledException" : "handledException"));
    }

    private JComponent getMessageComponent(){
        JScrollPane scrollPane = null;

        if (exception instanceof HtmlEnabledBusinessException) {
            htmlMessageField = new JTextPane();
            htmlMessageField.setEditable(false);
            htmlMessageField.setContentType("text/html");
            HTMLEditorKit editorKit = (HTMLEditorKit) htmlMessageField.getEditorKit();
            StyleSheet styles = editorKit.getStyleSheet();
            StringBuffer rule = new StringBuffer("body { font-family:");
            JLabel tmpLbl = new JLabel("Test");
            rule.append(tmpLbl.getFont().getFamily());
            rule.append(",arial; ");
            rule.append("font-size:");
            rule.append(tmpLbl.getFont().getSize());
            rule.append("}");
            styles.addRule(rule.toString());
            scrollPane = new JScrollPane(htmlMessageField);
        } else {
            messageField = new JTextArea();
            messageField.setRows(3);
            messageField.setLineWrap(true);
            messageField.setEditable(false);
            scrollPane = new JScrollPane(messageField);
        }
        scrollPane.setViewportBorder(createViewPortBorder());

        JPanel headerPnl = getHeaderPanel(TranslatorProvider.instance.get().getTranslator().getTranslation("errorMessage"), null);
        errorCodeLabel = new JLabel( TranslatorProvider.instance.get().getTranslator().getTranslation("errorCode")+": ");
        headerPnl.add(errorCodeLabel, BorderLayout.SOUTH);

        scrollPane.setColumnHeaderView(headerPnl);
        return scrollPane;
    }

    private JComponent getStackTraceComponent(){
        stackTraceField = new JTextArea();
        stackTraceField.setLineWrap(true);
        stackTraceField.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(stackTraceField);
        scrollPane.setViewportBorder(createViewPortBorder());
        scrollPane.setColumnHeaderView(getHeaderPanel(TranslatorProvider.instance.get().getTranslator().getTranslation("stackTrace"), null));

        stackTracePanel = new JPanel(new BorderLayout());
        stackTracePanel.add(scrollPane, BorderLayout.CENTER);

        return stackTracePanel;
    }

    private Border createViewPortBorder(){
        return BorderFactory.createEmptyBorder(3,3,3,3);
    }

    private JPanel getHeaderPanel(String text, Icon icon){
        JPanel panel = new JPanel(new BorderLayout(3,8));
        panel.setBorder(new EmptyBorder(3,3,3,3));

        JLabel headerLabel = new JLabel(text, icon, JLabel.LEADING);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));

        panel.add(headerLabel, BorderLayout.NORTH);
        return panel;
    }

    private JComponent getButtonComponent(){
        JButton exitButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("exitProgram"));
        exitButton.setMnemonic(KeyEvent.VK_E);
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(1);   // Exit with 1: an error occurred
            }
        });

        JButton closeButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("close"));
        closeButton.setMnemonic(KeyEvent.VK_C);
        closeButton.setEnabled(getDefaultCloseOperation() != WindowConstants.DO_NOTHING_ON_CLOSE);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });

        JButton copyButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("copy"));
        copyButton.setMnemonic(KeyEvent.VK_O);
        copyButton.setToolTipText(TranslatorProvider.instance.get().getTranslator().getTranslation("copyToClipboard"));
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Clipboard systemClipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringBuilder textSelection = new StringBuilder();
                textSelection.append(errorCodeLabel.getText());
                textSelection.append(System.getProperty("line.separator"));
                textSelection.append(System.getProperty("line.separator"));
                textSelection.append(stackTraceField.getText());
                systemClipBoard.setContents(new StringSelection(textSelection.toString()), null);
            }
        });

        detailsButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("details"));
        detailsButton.setMnemonic(KeyEvent.VK_D);
        detailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (stackTracePanel != null){
                    stackTracePanel.setVisible(!stackTracePanel.isVisible());
                    if (stackTracePanel.isVisible()){
                        setPreferredSize(previousSize);
                        detailsButton.setIcon(UP_ICON);
                    }else{
                        previousSize = ExceptionDialog.this.getSize();
                        setPreferredSize(null);
                        detailsButton.setIcon(DOWN_ICON);
                    }
                    pack();
                }
            }
        });
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 6, 0));

        buttonPanel.add(exitButton);
        buttonPanel.add(closeButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(detailsButton);

        if (exception.getCause() != null){
            JButton causeDetailsButton = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("causeDetails"));
            causeDetailsButton.setMnemonic(KeyEvent.VK_S);
            causeDetailsButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Throwable t = ExceptionDialog.this.exception;
                        while (t.getCause() != null) {
                            t = t.getCause();
                        }
                        ExceptionDialog cause = new ExceptionDialog();
                        cause.setTitle(cause.getTitle()+" [" + TranslatorProvider.instance.get().getTranslator().getTranslation("cause") + "]");
                        cause.handle(t);
                    }
                });
            buttonPanel.add(causeDetailsButton);
        }

        JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 2, 5));
        lowerPanel.add(buttonPanel);
        lowerPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        return lowerPanel;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Not interessted in
       }

        final ExceptionDialog dialog = new ExceptionDialog();
        dialog.setIconImage(((ImageIcon) EisIcons.EISERVER_ICON).getImage());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        Thread.setDefaultUncaughtExceptionHandler(dialog);

        StringBuilder s = new StringBuilder();
        for (int i = 1; i <= 40; i++) {
            s.append("Line " + i + "blablablablablabbfsqfdgsdgfsdfgsdf\n\r");
        }
        throw new RuntimeException(s.toString());
    }
}
