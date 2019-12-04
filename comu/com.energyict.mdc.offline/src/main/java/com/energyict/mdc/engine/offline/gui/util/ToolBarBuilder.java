package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideToggleButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

public class ToolBarBuilder {


    public static String PREFVALUE_NAVIGATION_TOOLBAR_FLOATING = "Floating";

    /**
     * Creates a <Code>JToolbar</Code>
     *
     * @param titleKey translationKey that will be used to set the toolbar's name
     * @return a new JToolbar
     */
    public static JToolBar createToolBar(String titleKey) {
        return createToolBar(titleKey, null, null, null);
    }

    /**
     * Creates a <Code>JToolbar</Code>
     *
     * @param titleKey       translationKey that will be used to set the toolbar's name
     * @param preferences     to store the toolbar's last position
     * @param preferencesKey preferences key used to store the toolbars last position
     *                       the position matches the <Code>BorderLayout</Code> constraints North,South....
     *                       If the prefkeyToolbarPosition is null, no preferences will be stored
     * @return a new JToolbar
     * @see BorderLayout
     */
    public static JToolBar createToolBar(String titleKey, final Container toolbarContainer, final Preferences preferences, final String preferencesKey) {
        final JToolBar toolbar = new JToolBar(TranslatorProvider.instance.get().getTranslator().getTranslation(titleKey));
        if (preferences != null) {
            if (toolbarContainer == null) {
                throw new IllegalArgumentException("toolbarContainer cannot be null");
            }
            if (preferencesKey == null) {
                throw new IllegalArgumentException("preferencesKey cannot be null");
            }
            // Storing the toolbar's position
            toolbar.addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    Object layoutConstraints = ((BorderLayout) toolbarContainer.getLayout()).getConstraints(toolbar);
                    if (layoutConstraints == null)  // When the toolbar is floating...
                    {
                        preferences.put(preferencesKey, PREFVALUE_NAVIGATION_TOOLBAR_FLOATING);
                    } else {
                        preferences.put(preferencesKey, layoutConstraints.toString());
                    }
                }
            });

        }
        return toolbar;
    }


    /**
     * Creates a default <Code>JideButton</Code> to use on toolbars
     *
     * @return a default <Code>JideButton</Code>
     */
    public static JideButton createToolBarButton() {
        JideButton button = new JideButton();
        setButtonDefaults(button);
        return button;
    }

    /**
     * Creates a default <Code>JideButton</Code> to use on toolbars
     *
     * @return a default <Code>JideButton</Code>
     * @parameter label the button's label
     */
    public static JideButton createToolBarButton(String label) {
        JideButton button = new JideButton(label);
        setButtonDefaults(button);
        return button;
    }

    /**
     * Creates a default <Code>JideButton</Code> to use on toolbars
     *
     * @return a default <Code>JideButton</Code>
     * @parameter action action that will be performed when clicking the button
     */
    public static JideButton createToolBarButton(AbstractAction action) {
        JideButton button = new JideButton(action);
        setButtonDefaults(button);
        return button;
    }


    /**
     * Creates a default <Code>JideToggleButton</Code> to use on toolbars
     *
     * @return a default <Code>JideToggleButton</Code>
     */
    public static JideToggleButton createToolBarToggleButton() {
        JideToggleButton button = new JideToggleButton();
        setButtonDefaults(button);
        return button;
    }

    /**
     * Creates a default <Code>JideButton</Code> to use on toolbars
     *
     * @return a default <Code>JideButton</Code>
     * @parameter label the button's label
     */
    public static JideToggleButton createToolBarToggleButton(String label) {
        JideToggleButton button = new JideToggleButton(label);
        setButtonDefaults(button);
        return button;
    }


    /**
     * Creates a default <Code>JideButton</Code> to use on toolbars
     *
     * @return a default <Code>JideButton</Code>
     * @parameter action action that will be performed when clicking the button
     */
    public static JideToggleButton createToolBarToggleButton(AbstractAction action) {
        JideToggleButton button = new JideToggleButton(action);
        setButtonDefaults(button);
        return button;
    }


    private static void setButtonDefaults(JideButton button) {
        button.setFocusable(false);
        button.setButtonStyle(JideButton.TOOLBAR_STYLE);
    }
}
