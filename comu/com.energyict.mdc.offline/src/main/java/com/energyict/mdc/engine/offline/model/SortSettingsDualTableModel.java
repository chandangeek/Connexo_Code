package com.energyict.mdc.engine.offline.model;

import com.energyict.mdc.engine.offline.gui.actions.SortInfo;
import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.jidesoft.grid.DefaultExpandableRow;

import javax.swing.*;


public class SortSettingsDualTableModel extends SortSettingsDualListModel {

    public SortSettingsDualTableModel(SortSettings settings){
        super(settings);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addSortInfo(SortInfo info){
        this.addElement(new SortInfoRow(info));
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void setSortInfoAt(SortInfo sortInfo, int row){
        this.setElementAt(new SortInfoRow(sortInfo), row);
    }

    @Override
    public SortInfo getSortInfo(int row){
        return ((SortInfoRow) this.getElementAt(row)).getSortInfo();
    }

    // For use within a Table
    public class SortInfoRow extends DefaultExpandableRow {
        private SortInfo sortInfo;

        SortInfoRow(SortInfo sortInfo){
            this.sortInfo = sortInfo;
        }

        public SortInfo getSortInfo(){
            return this.sortInfo;
        }


        public Object getValueAt(int column){
            switch (column){
                case 0: return this.sortInfo.getName();
                case 1: return this.sortInfo.getSortOrder();
                default: return null;
            }
        }

        public void setValueAt(Object value, int column){
            if (column == 1){
                this.sortInfo.setSortOrder((SortOrder) value);
            }
            super.setValueAt(value, column);
        }

    }

}
