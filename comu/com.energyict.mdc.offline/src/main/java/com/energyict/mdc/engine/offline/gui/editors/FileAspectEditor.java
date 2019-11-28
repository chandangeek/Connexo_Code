package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class FileAspectEditor extends AspectEditor<JPanel> implements DocumentListener {

    private JLabel jLabel;
    private JPanel editorPanel;
    private JTextComponent jValue;
    private JButton fileChooserButton;

    private int fileSelectionMode = JFileChooser.FILES_ONLY;
    private FileFilter filter;


    public FileAspectEditor() {
        jLabel = new JLabel();
        initValueComponent();
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public void setFileSelectionMode(int mode) {
        this.fileSelectionMode = mode;
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JPanel getValueComponent() {
        return editorPanel;
    }

    public void setDocument(Document document) {
        jValue.getDocument().removeDocumentListener(this);
        document.addDocumentListener(this);
        jValue.setDocument(document);
    }

    public File getFile() {
        return new File(jValue.getText());
    }

    protected void initValueComponent() {
        jValue = new JTextField(80);
        jValue.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateToolTip();
            }
        });
        jValue.getDocument().addDocumentListener(this);

        fileChooserButton = new JButton("...");
        fileChooserButton.setMargin(new Insets(0, 2, 0, 2));
        fileChooserButton.setToolTipText(TranslatorProvider.instance.get().getTranslator().getTranslation("clickToBrowse"));
        fileChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseForFile();
            }
        });

        editorPanel = new JPanel(new BorderLayout(2, 2));
        editorPanel.add(jValue, BorderLayout.CENTER);
        editorPanel.add(fileChooserButton, BorderLayout.EAST);
    }

    protected Object getViewValue() {
        return new File(jValue.getText());
    }

    protected void setViewValue(Object value) {
        if (value == null) {
            updateField("");
        } else {
            updateField(((File) value).getPath());
        }
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
        if (readOnly) {
            jValue.setCaretPosition(0);
        }
        fileChooserButton.setEnabled(!readOnly);
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e) {
        updateModel();
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e) {
        updateModel();
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent e) {
        updateModel();
    }

    protected boolean hasValidModel() {
        Object o = getModelValue();
        return o != null && ((String) o).trim().length() > 0;
    }

    protected void updateField(String text) {
        jValue.getDocument().removeDocumentListener(this);
        jValue.setText(text);
        jValue.getDocument().addDocumentListener(this);

        if (!jValue.isEditable()) {
            jValue.setCaretPosition(0);
        }
        updateToolTip();
    }

    private void updateToolTip() {
        if (!jValue.isVisible() || jValue.getText() == null) {
            jValue.setToolTipText(null);
            return;
        }
        String text = jValue.getText();
        FontMetrics fm = jValue.getFontMetrics(jValue.getFont());
        Rectangle rectText = fm.getStringBounds(text, jValue.getGraphics()).getBounds();
        Rectangle rectField = jValue.getBounds();
        Insets insets = jValue.getInsets();
        if (rectField.width - (insets.left + insets.right) < rectText.width) {
            jValue.setToolTipText(text);
        } else {
            jValue.setToolTipText(null);
        }
    }

    private void browseForFile() {
        File file = getFile();
        JFileChooser fc = new JFileChooser(file);
        fc.setFileSelectionMode(this.fileSelectionMode);
        if (this.filter != null) {
            fc.setFileFilter(filter);
        }
        if (file == null || !file.exists()) {
            fc.setCurrentDirectory(Utils.getOsUserHomeDirectory());
        } else {
            fc.setCurrentDirectory(file);
        }

        fc.setDialogTitle(TranslatorProvider.instance.get().getTranslator().getTranslation("browse"));
        fc.setAcceptAllFileFilterUsed(true);

        if (fc.showSaveDialog(UiHelper.getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            jValue.setText(file.getPath());
            jValue.setSelectionStart(0);
            jValue.setSelectionEnd(file.getPath().length() - 1);
            jValue.setCaretPosition(file.getPath().length());
        }

    }
}

