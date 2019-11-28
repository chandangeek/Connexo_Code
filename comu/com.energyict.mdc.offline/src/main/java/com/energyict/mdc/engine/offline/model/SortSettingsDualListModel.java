package com.energyict.mdc.engine.offline.model;

import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.jidesoft.list.DefaultDualListModel;

import javax.swing.*;

/**
 * DualListModel for setting the {@link SortSettings} {@link SortInfo}
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 13:19
 */
public class SortSettingsDualListModel extends DefaultDualListModel {

    private SortSettings sortSettings;

    public SortSettingsDualListModel(SortSettings settings){
        this.sortSettings = new SortSettings(settings.getColumnNames(), settings.getAllColumnNames());
        for (String columnName: settings.getColumnNames()){
            this.addSortInfo(new SortInfo(columnName, SortOrder.UNSORTED));
        }
        for (SortInfo sortInfo: settings.getSortInfo()){
            this.sortSettings.addToSort(sortInfo);
            int index = getIndexFor(sortInfo.getName());
            if (index >= 0){
                setSortInfoAt(sortInfo,index);
                super.addSelectionInterval(index, index);
            }
        }
    }

    public SortSettings getSortSettings(){
        return this.sortSettings;
    }

    @Override
    public void clearSelection(){
        super.clearSelection();
        this.sortSettings.removeAllFromSort();
    }
    @Override
    public void addSelectionInterval(int i, int i1) {
        super.addSelectionInterval(i, i1);
        for (int j = i; j <= Math.min(i1,this.size()); j++){
            if (isSelectedIndex(j)){
                SortInfo info = getSortInfo(j);
                info.setSortOrder(SortOrder.ASCENDING);
                this.sortSettings.addToSort(info);
            }
        }
    }
    @Override
    public void removeSelectionInterval(int i, int i1) {
        for (int j = i; j <= Math.min(i1,this.size()); j++){
            SortInfo info = getSortInfo(this.getSelectedIndices()[j]);
            info.setSortOrder(SortOrder.UNSORTED);
            this.sortSettings.removeFromSort(info);
        }
        super.removeSelectionInterval(i, i1);
    }
    @Override
    public void moveSelection(int i, int i1, int i2, boolean b) {
        super.moveSelection(i, i1, i2, b);
        sortSettings.removeAllFromSort();
        for (int j: getSelectedIndices()){
            sortSettings.addToSort(this.getSortInfo(j));
        }
    }

    protected SortInfo getSortInfo(int row){
        return (SortInfo) this.getElementAt(row);
    }
    @SuppressWarnings("unchecked")
    protected void setSortInfoAt(SortInfo sortInfo, int row){
        this.setElementAt(sortInfo, row);
    }

    private int getIndexFor(String name){
        if (name != null){
            for (int i=0; i<this.size(); i++){
                SortInfo sortInfo =  getSortInfo(i);
                if (name.equals(sortInfo.getName())){
                    return i;
                }
            }
        }
        return -1;
    }
    @SuppressWarnings("unchecked")
    protected void addSortInfo(SortInfo info){
        this.addElement(info);
    }

}
