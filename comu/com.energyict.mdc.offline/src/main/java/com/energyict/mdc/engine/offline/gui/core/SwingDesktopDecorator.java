package com.energyict.mdc.engine.offline.gui.core;


import com.energyict.mdc.engine.offline.DesktopDecorator;
import com.energyict.mdc.engine.offline.gui.EisMenuBarBuilder;
import com.energyict.mdc.engine.offline.gui.actions.EisAbstractAction;
import com.energyict.mdc.engine.offline.gui.models.Navigator;

import javax.swing.*;

public interface SwingDesktopDecorator extends DesktopDecorator {

    /**
     * Returns the icon of the decorated BusinessObject
     *
     * @return the icon of the decorated BusinessObject
     */
    Icon getIcon();

    /**
     * Return the appropriate <Code>JPanel</Code> when 'browsing' the decorated BusinessObject
     *
     * @param navigator : BrowseModel keeping the state of navigation
     * @return the appropriate <Code>JPanel</Code> when 'browsing' the decorated BusinessObject
     */
    JPanel getBrowsePanel(Navigator navigator);

    /**
     * Return the appropriate <Code>EisMenuBarBuilder</Code> when 'browsing' the decorated BusinessObject
     *
     * @param navigator : BrowseModel keeping the state of navigation
     * @return the appropriate <Code>EisMenuBarBuilder</Code> when 'browsing' the decorated BusinessObject
     */
    EisMenuBarBuilder getMenuBarBuilder(Navigator navigator);

    /**
     * Returns a String that can be used as title for the browser <Code>InternalFrame</Code>
     *
     * @return the icon of the decorated BusinessObject
     */
    String getFrameTitle();

    /**
     * Returns an alternative action for the 'navigate to the decorated object'
     *
     * @ an <Code>EisAbstractAction</Code> that is performed instead of navigating to the decorated object
     * Is used objects like UserFile where a navigateTo has no sense, but where the navigate to is replaced by opening the userfile
     */
    EisAbstractAction getAlternativeNavigationAction();

}
