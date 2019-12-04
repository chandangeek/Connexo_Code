package com.energyict.mdc.engine.offline.gui.core;

import com.energyict.mdc.engine.offline.MdwIcons;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.energyict.mdc.engine.offline.gui.windows.SortSettingsDualTablePanel;
import com.energyict.mdc.engine.offline.model.SortSettingsDualTableModel;
import com.jidesoft.grid.ISortableTableModel;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.SortableTableModel;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 8:56
 */
public class SortableTableSortSettingsAction extends AbstractAction {

    private final static String NAME = "sortSettings";
    private final static String DIALOG_TITLE = "chooseSortSettings";

    private SortableTable table;

    public SortableTableSortSettingsAction(SortableTable table) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation(NAME) + "...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.SMALL_ICON, MdwIcons.SORTING_ICON);
        this.table = table;
    }

    public void actionPerformed(ActionEvent event) {
        if (!table.isSortable()){
            return;
        }
        SortSettings settings = fromTableModel();

        SortSettingsDualTableModel model = new SortSettingsDualTableModel(settings);
        SortSettingsDualTablePanel panel = new SortSettingsDualTablePanel(model);
        UiHelper.showModalDialog(panel, TranslatorProvider.instance.get().getTranslator().getTranslation(DIALOG_TITLE));

        if (!panel.isCanceled()) {
            updateTableModel(model.getSortSettings());
        }
    }

    private SortSettings fromTableModel(){
        SortableTableModel tableModel = (SortableTableModel) table.getModel();
        SortSettings sortSettings = new SortSettings();
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        List<String> columnNames = new ArrayList<>();
        int i=0;
        while (columns.hasMoreElements()){
            if (tableModel.isColumnSortable(i++)){
                String header = (String)columns.nextElement().getHeaderValue();
                if (!Utils.isNull(header)){
                    columnNames.add(header);
                }
            }
        }
        sortSettings.setColumnNames(columnNames);
        for (ISortableTableModel.SortItem sortItem: tableModel.getSortingColumns()){
            SortInfo sortInfo = new SortInfo(tableModel.getColumnName(sortItem.getColumn()), (sortItem.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING ));
            sortSettings.addToSort(sortInfo);
        }
        return sortSettings;
    }

    private void updateTableModel(SortSettings settings){
        table.unsort();
        if (settings.getSortInfo().isEmpty()){
            table.setMultiColumnSortable(false);    //only multi column sort using the 'sort settings' action in table header
        }else{
            table.setMultiColumnSortable(true);


            for (SortInfo sortInfo : settings.getSortInfo()){
                int columnIndex = table.getColumnModel().getColumnIndex(sortInfo.getName());
                table.setShowSortOrderNumber(false);
                table.sortColumn(columnIndex, false, sortInfo.getSortOrder() == SortOrder.ASCENDING);
            }
        }
    }

}
