/*
 * SortingSettingsAction.java
 *
 * Created on 13 december 2004, 13:16
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;
import com.energyict.mdc.engine.offline.gui.windows.SortingSettingsPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Geert
 */
public class SortingSettingsAction extends AbstractAction {

    private TableBubbleSortDecorator sorter;
    private SortSettings settings;
    public static final String PARENTFRAME = "parentFrame";

    /**
     * Creates a new instance of SortingSettingsAction
     */
    public SortingSettingsAction(TableBubbleSortDecorator sorter) {
        this.sorter = sorter;
        this.settings = sorter.getSortSettings();
        setEnabled(sorter.isActive());
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("sortSettings") + "...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.SMALL_ICON, MdwIcons.SORTING_ICON);
    }

    public void actionPerformed(ActionEvent event) {
        if (getFrame() == null) {
            return;
        }
        sorter.rememberSortSettings();
        //reinitialize sort settings each time an action is performed, since the settings object might be recreated.
        settings = sorter.getSortSettings();
        SortingSettingsPnl panel = new SortingSettingsPnl(settings);
        UiHelper.showModalDialog(panel, TranslatorProvider.instance.get().getTranslator().getTranslation("chooseSortSettings"));

        if (panel.isCanceled()) {
            sorter.rollbackToMarkedSortSettings();
        } else {
            sorter.sortAndRefresh();
        }
    }

    private Frame getFrame() {
        return (Frame) (UserEnvironment.getDefault().get(PARENTFRAME));
    }
}
