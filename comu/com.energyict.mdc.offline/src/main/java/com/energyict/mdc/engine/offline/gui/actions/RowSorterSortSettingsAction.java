package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.windows.SortSettingsDualTablePanel;
import com.energyict.mdc.engine.offline.model.SortSettingsDualTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 8:56
 */
public class RowSorterSortSettingsAction extends AbstractAction {

    private final static String NAME = "sortSettings";
    private final static String DIALOG_TITLE = "chooseSortSettings";

    private JTable table;

    public RowSorterSortSettingsAction(JTable table) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation(NAME) + "...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.SMALL_ICON, MdwIcons.SORTING_ICON);
        this.table = table;
    }

    public void actionPerformed(ActionEvent event) {
        TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
        if (sorter == null){
            return;
        }
        SortSettings settings = new SortSettings(sorter);

        SortSettingsDualTableModel model = new SortSettingsDualTableModel(settings);
        SortSettingsDualTablePanel panel = new SortSettingsDualTablePanel(model);
        UiHelper.showModalDialog(panel, TranslatorProvider.instance.get().getTranslator().getTranslation(DIALOG_TITLE));

        if (!panel.isCanceled()) {
            model.getSortSettings().updateRowSorter(sorter);
        }
    }

}
