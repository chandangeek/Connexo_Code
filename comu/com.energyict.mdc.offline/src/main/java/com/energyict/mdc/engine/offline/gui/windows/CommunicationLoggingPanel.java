package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.decorators.EventDecorator;
import com.energyict.mdc.engine.offline.gui.decorators.ReadWriteEventDecorator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author sva
 * @since 10/03/14 - 13:56
 */
public class CommunicationLoggingPanel extends JPanel {

    private static String FONT_MONOSPACED = "Monospaced";

    private JSplitPane loggingSplitPane;
    private JPanel loggingPnl;
    private JScrollPane loggingScrollPane;
    private JTextArea loggingTextArea;

    private JPanel buttonPnl;
    private JButton clearBtn;
    private JButton copyLoggingBtn;
//    private JButton exitBtn;

    private JPanel tracePnl;
    private JSplitPane traceSplitPane;
    private JScrollPane traceHexScrollPane;
    private JTextArea traceHexTextArea;
    private JScrollPane traceAsciiScrollPane;
    private JTextArea traceAsciiTextArea;
    private StringBuilder traceStringBuilder;

//    private CommunicationLoggingDialog parentDialog;

    private final Object lock = new Object();   // Lock to prevent concurrent modifications of the different text areas
                                                // as all operations should be done in group (for loggingTextArea, traceHexTextArea, traceAsciiScrollPane)

    public CommunicationLoggingPanel(/*CommunicationLoggingDialog parentDialog*/) {
//        this.parentDialog = parentDialog;
        initializePnl();
    }

    private void initializePnl() {
        this.setLayout(new BorderLayout());
        this.add(getLoggingSplitPane(), BorderLayout.CENTER);
        this.add(getRightAlignedButtonPnl(), BorderLayout.SOUTH);
    }

    private JSplitPane getLoggingSplitPane() {
        if (loggingSplitPane == null) {
            loggingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            loggingSplitPane.setResizeWeight(0.70);
            loggingSplitPane.setOneTouchExpandable(true);
            loggingSplitPane.setMinimumSize(new Dimension(100, 150));

            loggingSplitPane.add(getLoggingPnl());
            loggingSplitPane.add(getTracePnl());
        }
        return loggingSplitPane;
    }

    private JPanel getLoggingPnl() {
        if (loggingPnl == null) {
            loggingPnl = new JPanel(new BorderLayout());
            loggingPnl.setBorder(BorderFactory.createTitledBorder(UiHelper.translate("mmr.logging")));
            loggingPnl.add(getLoggingScrollPane(), BorderLayout.CENTER);
        }
        return loggingPnl;
    }

    private JScrollPane getLoggingScrollPane() {
        if (loggingScrollPane == null) {
            DefaultCaret caret = (DefaultCaret) getLoggingTextArea().getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            loggingScrollPane = UiHelper.createNewJScrollPane();
            loggingScrollPane.setViewportView(getLoggingTextArea());
        }
        return loggingScrollPane;
    }

    private JTextArea getLoggingTextArea() {
        if (loggingTextArea == null) {
            loggingTextArea = new JTextArea();
            loggingTextArea.setEditable(false);
            loggingTextArea.setBackground(this.getBackground());
            loggingTextArea.setFont(new Font(loggingTextArea.getFont().getName(), Font.PLAIN, 12));
        }
        return loggingTextArea;
    }

    private JPanel getTracePnl() {
        if (tracePnl == null) {
            tracePnl = new JPanel(new BorderLayout());
            tracePnl.setBorder(BorderFactory.createTitledBorder(UiHelper.translate("mmr.trace")));
            tracePnl.add(getTraceSplitPane(), BorderLayout.CENTER);
        }
        return tracePnl;
    }

    private JSplitPane getTraceSplitPane() {
        if (traceSplitPane == null) {
            traceSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            traceSplitPane.setResizeWeight(0.70);
            traceSplitPane.setOneTouchExpandable(true);
            traceSplitPane.setMinimumSize(new Dimension(100, 150));

            traceSplitPane.add(getTraceHexScrollPane());
            traceSplitPane.add(getTraceAsciiScrollPane());
            getTraceHexScrollPane().getVerticalScrollBar().setModel(getTraceAsciiScrollPane().getVerticalScrollBar().getModel());
        }
        return traceSplitPane;
    }

    private JScrollPane getTraceHexScrollPane() {
        if (traceHexScrollPane == null) {
            DefaultCaret caret = (DefaultCaret) getTraceHexTextArea().getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            traceHexScrollPane = UiHelper.createNewJScrollPane();
            traceHexScrollPane.setViewportView(getTraceHexTextArea());
        }
        return traceHexScrollPane;
    }

    private JTextArea getTraceHexTextArea() {
        if (traceHexTextArea == null) {
            traceHexTextArea = new JTextArea();
            traceHexTextArea.setEditable(false);
            traceHexTextArea.setBackground(this.getBackground());
            traceHexTextArea.setFont(new Font(FONT_MONOSPACED, Font.PLAIN, 12));
        }
        return traceHexTextArea;
    }

    private JScrollPane getTraceAsciiScrollPane() {
        if (traceAsciiScrollPane == null) {
            DefaultCaret caret = (DefaultCaret) getTraceAsciiTextArea().getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            traceAsciiScrollPane = UiHelper.createNewJScrollPane();
            traceAsciiScrollPane.setViewportView(getTraceAsciiTextArea());
        }
        return traceAsciiScrollPane;
    }

    private JTextArea getTraceAsciiTextArea() {
        if (traceAsciiTextArea == null) {
            traceAsciiTextArea = new JTextArea();
            traceAsciiTextArea.setEditable(false);
            traceAsciiTextArea.setBackground(this.getBackground());
            traceAsciiTextArea.setFont(new Font(FONT_MONOSPACED, Font.PLAIN, 12));
        }
        return traceAsciiTextArea;
    }

    public StringBuilder getTraceStringBuilder() {
        if (traceStringBuilder == null) {
            traceStringBuilder = new StringBuilder();
        }
        return traceStringBuilder;
    }

    private JPanel getRightAlignedButtonPnl() {
        JPanel alignRightPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        alignRightPnl.add(getButtonPnl());
        return alignRightPnl;
    }

    private JPanel getButtonPnl() {
        if (this.buttonPnl == null) {
            this.buttonPnl = new JPanel();
            this.buttonPnl.add(getCopyLoggingBtn());
            this.buttonPnl.add(getClearBtn());
//            this.buttonPnl.add(getExitBtn());
        }
        return this.buttonPnl;
    }

    public JButton getClearBtn() {
        if (this.clearBtn == null) {
            this.clearBtn = new JButton(com.energyict.mdc.engine.offline.gui.UiHelper.translate("clear"));
            this.clearBtn.setMnemonic(KeyEvent.VK_R);
            this.clearBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    synchronized (lock) {
                        getLoggingTextArea().setText(null);
                        getTraceHexTextArea().setText(null);
                        getTraceAsciiTextArea().setText(null);
                        getTraceStringBuilder().setLength(0);
                    }
                }
            });
        }
        return this.clearBtn;
    }

    public JButton getCopyLoggingBtn() {
        if (this.copyLoggingBtn == null) {
            this.copyLoggingBtn = new JButton(com.energyict.mdc.engine.offline.gui.UiHelper.translate("copyLoggingToClipboard"));
            this.copyLoggingBtn.setMnemonic(KeyEvent.VK_L);
            this.copyLoggingBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    copyCommunicationLoggingToClipboard();
                }
            });
        }

        return copyLoggingBtn;
    }

//    public JButton getExitBtn() {
//        if (this.exitBtn == null) {
//            this.exitBtn = new JButton(com.energyict.mdc.engine.offline.gui.UiHelper.translate("close"));
//            this.exitBtn.setMnemonic(KeyEvent.VK_C);
//            this.exitBtn.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
////                    getParentDialog().dispose();
//                }
//            });
//        }
//        return this.exitBtn;
//    }

    public void notifyOfComServerMonitorEvent(EventDecorator event) {
        synchronized (lock) {
            if (event instanceof ReadWriteEventDecorator) {
                String hexPart = ((ReadWriteEventDecorator) event).logHexPartReadWriteEvent();
                getTraceHexTextArea().append(hexPart);
                getTraceHexTextArea().append(System.lineSeparator());
                getTraceStringBuilder().append(hexPart);
                getTraceStringBuilder().append(System.lineSeparator());

                String asciiPart = ((ReadWriteEventDecorator) event).logAsciiPartReadWriteEvent();
                getTraceAsciiTextArea().append(asciiPart);
                getTraceAsciiTextArea().append(System.lineSeparator());
                getTraceStringBuilder().append(asciiPart);
                getTraceStringBuilder().append(System.lineSeparator());
            } else {
                getLoggingTextArea().append(event.asLogString());
                getLoggingTextArea().append(System.lineSeparator());
            }
        }
    }

    public void copyCommunicationLoggingToClipboard() {
        StringBuilder builder = new StringBuilder();
        builder.append("Communication logging:");
        builder.append(System.lineSeparator());
        builder.append(getLoggingTextArea().getText());
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("Communication trace:");
        builder.append(System.lineSeparator());
        builder.append(getTraceStringBuilder().toString());

        StringSelection selection = new StringSelection(builder.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        JOptionPane.showMessageDialog(getRootPane(), UiHelper.translate("mmr.communicationLoggingIsCopiedToTheClipboard"));
    }
}