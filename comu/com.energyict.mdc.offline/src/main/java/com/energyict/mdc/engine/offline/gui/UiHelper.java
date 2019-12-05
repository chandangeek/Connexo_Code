package com.energyict.mdc.engine.offline.gui;

import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.engine.offline.gui.panels.OptionsPnl;
import com.energyict.mdc.engine.offline.gui.util.EisConst;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.energyict.mdc.engine.offline.gui.windows.CompletionCodeDialog;
import com.energyict.mdc.engine.offline.gui.windows.ExceptionMsgPnl;
import com.energyict.mdc.engine.offline.gui.windows.IconProvider;
import com.energyict.mdc.engine.offline.model.CustomCompletionCode;
import com.sqlly.components.textpane.AttributeGroups;
import com.sqlly.components.textpane.LexicalGroupAttributes;
import com.sqlly.components.textpane.LexicalGroups;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

public class UiHelper {

    public static final int SCROLLABLE_UNIT_INCREMENT = 15;

    private static String fonts[] = {
            "TextField.font", // used as standard size
            "CheckBox.font", "Tree.font", "Viewport.font", "ProgressBar.font",
            "RadioButtonMenuItem.font", "ToolBar.font", "ToggleButton.font",
            "MenuItem.acceleratorFont", "Panel.font", "Menu.font",
            "TableHeader.font", "OptionPane.font", "MenuBar.font",
            "Button.font", "Label.font", "OptionPane.buttonFont",
            "ScrollPane.font", "MenuItem.font", "ToolTip.font",
            "List.font", "OptionPane.messageFont", "EditorPane.font",
            "Table.font", "TabbedPane.font", "RadioButton.font",
            "CheckBoxMenuItem.font", "TextPane.font", "PopupMenu.font",
            "TitledBorder.font", "ComboBox.font",
            "FormattedTextField.font", "ColorChooser.font", "TextArea.font",
            "RadioButtonMenuItem.acceleratorFont", "Spinner.font",
            "Menu.acceleratorFont", "CheckBoxMenuItem.acceleratorFont",
            "PasswordField.font",
            "JideButton.font", // added for the change user group button in the upper right corner
            "InternalFrame.titleFont"};
    private static int fontOffsets[] = new int[fonts.length];
    private static boolean fontOffsetsInitialized = false;

    public static int getFontSize() {
        Preferences userPrefs = Preferences.userNodeForPackage(OptionsPnl.class);
        return userPrefs.getInt(EisConst.PREFKEY_FONTSIZE, 11);
    }

    public static void applyFontSize() {
        applyFontSize(getFontSize());
    }

    private static void initFontOffsets() {
        if (fontOffsetsInitialized) {
            return;
        }
        UIDefaults uiDefaults = UIManager.getDefaults();
        // Use the size of the first font as the reference size:
        int standardFontSize = uiDefaults.getFont(fonts[0]).getSize();
        for (int i = 0; i < fonts.length; i++) {
            Font f = uiDefaults.getFont(fonts[i]);
            if (f != null) {
                fontOffsets[i] = f.getSize() - standardFontSize;
            } else {
                fontOffsets[i] = 0;
            }
        }
        fontOffsetsInitialized = true;
    }

    private static void applyFontSize(int fontSize) {
        // Cf. http://forum.java.sun.com/thread.jspa?forumID=57&threadID=560316
        if (!fontOffsetsInitialized) {
            initFontOffsets();
        }
        UIDefaults uiDefaults = UIManager.getDefaults();
        for (int i = 0; i < fonts.length; i++) {
            Font f = uiDefaults.getFont(fonts[i]);
            if (f != null) {
                uiDefaults.put(fonts[i], new FontUIResource(
                        f.deriveFont(f.getStyle(), fontSize + fontOffsets[i])));
            }
        }

        // Setting the fonts for the jcodeedit component
        Integer keys[] = {
                AttributeGroups.GROUP_DEFAULT,
                AttributeGroups.GROUP_RESERVED_WORDS,
                AttributeGroups.GROUP_ROUND_BRACKET,
                AttributeGroups.GROUP_BRACE,
                AttributeGroups.GROUP_DOUBLE,
                AttributeGroups.GROUP_INTEGER,
                AttributeGroups.GROUP_STRING,
                AttributeGroups.GROUP_COMMENTS,
                AttributeGroups.GROUP_CONTEXT
        };
        for (int i = 0; i < keys.length; i++) {
            LexicalGroupAttributes att = (LexicalGroupAttributes)
                    LexicalGroups.groupAttributes.get(keys[i]);
            att.font = att.font.deriveFont((float) (fontSize + 1));
        }

        Font font = (Font) LexicalGroups.fonts.remove(0);
        font = font.deriveFont((float) (fontSize + 1));
        LexicalGroups.fonts.add(font);
    }

    /**
     * 'Empty' mouseAdapter to use in combination with wait cursor
     */
    private final static MouseAdapter waitCursorMouseAdapter = new MouseAdapter() {
    };

    public static void showModalDialog(JPanel panel, String strTitle) {
        // Mantis #2591: the new dialog must always be on top
        JFrame tmpFrame = (JFrame) UserEnvironment.getDefault().get(OfflineFrame.class.getName());
        if (tmpFrame == null) {
            tmpFrame = new JFrame();
            tmpFrame.setIconImage(((ImageIcon) EisIcons.EISERVER_ICON).getImage());
        }
        EisDialog dlg = new EisDialog(tmpFrame, strTitle, true);
        if (panel instanceof IconProvider && ((IconProvider) panel).getIcon() instanceof ImageIcon) {
            dlg.setIconImage(((ImageIcon) ((IconProvider) panel).getIcon()).getImage());
        }
        dlg.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.weightx = constr.weighty = 1.0;
        constr.fill = GridBagConstraints.BOTH;
        constr.gridx = constr.gridy = 0;
        dlg.getContentPane().add(panel, constr);
        setDefaultButtonInDialog(dlg, panel);
        dlg.pack();
        dlg.setLocationRelativeTo(null); // center

        // Mantis #2591
        dlg.toFront();
        dlg.setVisible(true);
    }

    public static void showModalDialog(JFrame frame, JPanel panel, String strTitle) {
        EisDialog dlg = new EisDialog(frame, strTitle, true);
        if (panel instanceof IconProvider && ((IconProvider) panel).getIcon() instanceof ImageIcon) {
            dlg.setIconImage(((ImageIcon) ((IconProvider) panel).getIcon()).getImage());
        }
        dlg.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.weightx = constr.weighty = 1.0;
        constr.fill = GridBagConstraints.BOTH;
        constr.gridx = constr.gridy = 0;
        dlg.getContentPane().add(panel, constr);
        setDefaultButtonInDialog(dlg, panel);
        dlg.pack();
        dlg.setLocationRelativeTo(null); // center

        dlg.toFront();
        dlg.setVisible(true);
    }

    // ------------------------------------------------------------
    /*
     * Sets the default button of the dialog
     * (tries to use getDefaultButton() of the given JPanel)
     */

    public static void setDefaultButtonInDialog(JDialog dlg, JPanel panel) {
        Method method = null;
        try {
            method = panel.getClass().getMethod("getDefaultButton", new Class[]{});
            if (method != null) {
                try {
                    JButton btn = (JButton) method.invoke(panel, (Object[]) null);
                    dlg.getRootPane().setDefaultButton(btn);
                    return;
                } catch (Exception e) {
                    return;
                }
            }
        } catch (NoSuchMethodException e) {
            return;
        }
    }

    public static String translate(String key) {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(key);
    }

    public static Preferences getUserPreferences() {
        // startNode : com.energyict.mdc.engine.offline.gui
        Preferences start = Preferences.userNodeForPackage(UiHelper.class);
        // ParentNode : com.energyict.mdc.engine.offline
        Preferences parent = start.parent();
        // UserPrefs: registrykey = (JavaSoft.Prefs.)com.energyict.mdc.engine.offline.userprefs
        return parent.node(EisConst.PREFKEY_USERPREFS_NODENAME);
    }

    public static Preferences getAdditionalUserPreferences() {
        // startNode : com.energyict.mdwswing.windows
        Preferences start = Preferences.userNodeForPackage(UiHelper.class);
        // ParentNode : com.energyict.mdwswing
        Preferences parent = start.parent();
        // UserPrefs: registrykey = (JavaSoft.Prefs.)com.energyict.mdwswing.userprefs
        return parent.node(EisConst.PREFKEY_USERPREFS_NODENAME);
    }

    public static GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.insets = new Insets(2, 2, 2, 2);
        return gbc;
    }

    public static GridBagConstraints createGbc(int x, int y, int gridWidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gridWidth;
        gbc.gridheight = 1;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.insets = new Insets(2, 2, 2, 2);
        return gbc;
    }

    public static GridBagConstraints createGbc(int x, int y, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.insets = insets;
        return gbc;
    }

    public static void disableComponentAndAllChildren(JComponent component) {
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                disableComponentAndAllChildren((JComponent) child);
            }
        }
        component.setEnabled(false);
    }

    public static void enableComponentAndAllChildren(JComponent component) {
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                enableComponentAndAllChildren((JComponent) child);
            }
        }
        component.setEnabled(true);
    }

    public static void enableComponentAndAllChildren(JComponent component, boolean enable) {
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                enableComponentAndAllChildren((JComponent) child, enable);
            }
        }
        component.setEnabled(enable);
    }

    public static void setComponentAndChildrenOpaque(JComponent component, boolean opaque) {
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                setComponentAndChildrenOpaque((JComponent) child, opaque);
            }
        }
        component.setOpaque(opaque);
    }

    public static void setForegroundOfComponentAndChildren(JComponent component, Color color) {
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                setForegroundOfComponentAndChildren((JComponent) child, color);
            }
        }
        component.setForeground(color);
    }

    /**
     * Creates a new JScrollPane and sets the UnitIncrement of the VerticalScrollBar
     *
     * @return the newly created JScrollPane
     */
    public static JScrollPane createNewJScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        return scrollPane;
    }

    /**
     * Creates a new JScrollPane for the given Component and sets the UnitIncrement of the VerticalScrollBar
     *
     * @return the newly created JScrollPane
     */
    public static JScrollPane createNewJScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLLABLE_UNIT_INCREMENT);
        return scrollPane;
    }

    public static boolean reportException(final Throwable ex) {
        return reportException(ex, null);
    }

    public static boolean reportException(final Throwable ex, JFrame jFrame) {
        return reportException(ex, false, jFrame);
    }

    public static boolean reportException(final Throwable ex, boolean cancelPossible, JFrame jFrame) {
        return reportException(ex, cancelPossible, UIManager.getIcon("OptionPane.errorIcon"), jFrame);
    }

    public static boolean reportException(final Throwable ex, boolean cancelPossible, Icon icon, JFrame jFrame) {
        return reportException(ex, cancelPossible, icon, TranslatorProvider.instance.get().getTranslator().getTranslation("errorMessage"), jFrame);
    }

    public static boolean reportException(final Throwable ex, boolean cancelPossible, Icon icon, String dialogTitle, JFrame jFrame) {
        String message = getLocalizedMessage(ex);
        ExceptionMsgPnl thePanel = new ExceptionMsgPnl(message, ex, cancelPossible, icon);
        if (jFrame == null) {
            jFrame = new JFrame();
            jFrame.setIconImage(((ImageIcon) EisIcons.EISERVER_ICON).getImage());
        }
        showModalDialog(jFrame, thePanel, dialogTitle);
        return thePanel.userWantsToCancel();
    }

    private static String getLocalizedMessage(final Throwable ex) {
        String message = getExceptionMessage(ex);
        if (Utils.isNull(message)) {
            Throwable ex2 = ex.getCause();
            if (ex2 == null) {
                message = ex.toString();
            } else {
                message = getExceptionMessage(ex2);
                if (Utils.isNull(message)) {
                    message = ex.toString();
                }
            }
        }
        return message;
    }

    private static String getExceptionMessage(Throwable ex) {
        return ex.getLocalizedMessage();
    }

    public static CustomCompletionCode getCustomCompletionCode() {
        CompletionCodeDialog completionCodeDialog =
            new CompletionCodeDialog(getMainWindow(), getMainWindow().getCompletionCodes(), true);
        completionCodeDialog.setVisible(true);
        return completionCodeDialog.getCustomCompletionCode();
    }

    public static OfflineFrame getMainWindow() {
        return (OfflineFrame) UserEnvironment.getDefault().get(OfflineFrame.class.getName());
    }

    public static void setDefaultFocusInWindow(Component defaultComponentToFocus) {
        if (defaultComponentToFocus == null) {
            return;
        }
        Window window = SwingUtilities.windowForComponent(defaultComponentToFocus);
        if (window != null) {
            InitialFocusSetter.setInitialFocus(window, defaultComponentToFocus);
        }
    }

    public static Color getDefaultBackGroundSelectionColor() {
        UIDefaults defaults = UIManager.getDefaults();
        Color color = (Color) defaults.get("Tree.selectionBackground");
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

    public static Color getDefaultForeGroundSelectionColor() {
        UIDefaults defaults = UIManager.getDefaults();
        Color color = (Color) defaults.get("Tree.selectionForeground");
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

    public static Color getDefaultBackGroundColor() {
        UIDefaults defaults = UIManager.getDefaults();
        Color color = (Color) defaults.get("Tree.background");
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

    public static Color getDefaultForeGroundColor() {
        UIDefaults defaults = UIManager.getDefaults();
        Color color = (Color) defaults.get("Tree.foreground");
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

    public static String limitLengthOfLabel(String text, int length) {
        return limitLengthOfLabel(text, length, "");
    }

    public static String limitLengthOfLabel(String text, int length, String defaultValue) {
        if (text == null || text.length() == 0) {
            return defaultValue;
        }

        if (text.length() > length) {
            text = text.substring(0, length - 2) + "..";
        }
        return text;
    }

    public static void startWaitCursor() {
        Component glassPane = UiHelper.getMainWindow().getGlassPane();
        if (!glassPane.isVisible()) {
            glassPane.addMouseListener(waitCursorMouseAdapter);
            glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glassPane.setVisible(true);
        }
    }

    public static void stopWaitCursor() {
        Component glassPane = UiHelper.getMainWindow().getGlassPane();
        if (glassPane.isVisible()) {
            glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            glassPane.removeMouseListener(waitCursorMouseAdapter);
            glassPane.setVisible(false);
        }
    }

    static class InitialFocusSetter {

        public static void setInitialFocus(Window w, Component c) {
            w.addWindowListener(new FocusSetter(c));
        }

        public static class FocusSetter extends WindowAdapter {

            Component initComp;

            FocusSetter(Component c) {
                initComp = c;
            }

            public void windowOpened(WindowEvent e) {
                initComp.requestFocus();

                // Since this listener is no longer needed, remove it
                e.getWindow().removeWindowListener(this);
            }
        }
    }

}