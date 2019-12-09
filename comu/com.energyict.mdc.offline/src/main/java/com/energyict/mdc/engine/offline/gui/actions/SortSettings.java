package com.energyict.mdc.engine.offline.gui.actions;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;


public class SortSettings implements RowSorterListener{

    private List<String> allColumnNames = new ArrayList<>(); // Strings (col names)
    private List<String> columnNames = new ArrayList<>(); // Strings (col names)
    private List<SortInfo> sortInfo = new ArrayList<>(); // SortInfo objects

    private TableRowSorter<? extends TableModel> sorter;

    /**
     * Creates a new instance of SortSettings
     */
    public SortSettings() {
        this(new ArrayList<String>());
    }

    public SortSettings(List<String> names) {
        setColumnNames(names);
    }

    public SortSettings(List<String> names, List<String> allColumnNames) {
        setColumnNames(names);
        this.allColumnNames = new ArrayList<>(allColumnNames);
    }

    public SortSettings(TableRowSorter<? extends TableModel> sorter){
        this.sorter = sorter;
        initColumnNames();
        updateSortInfo();
        sorter.addRowSorterListener(this);
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED){
            updateSortInfo();
        }
    }

    public void setColumnNames(List<String> names) {
        columnNames = names;
        sortInfo.clear();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getSortColumnNames() {
        List<String> result = new ArrayList<>();
        for (SortInfo each : sortInfo) {
            result.add(each.getName());
        }
        return result;
    }

    public void addIndexToSortOn(int index) {
        if (index >= getAllColumnNames().size() || index < 0) {
            return;
        }
        List<Integer> indices = new ArrayList<>();
        indices.add(index);
        setColumnIndicesSortedOn(indices);
    }

    public void addIndexToSortOn(int index, SortOrder sortOrder) {
        if (sortOrder == SortOrder.UNSORTED){
            throw new IllegalArgumentException("SortOrder should be SortOrder.ASCENDING or SortOrder.DESCENDING");
        }
        if (index >= getAllColumnNames().size() || index < 0) {
            return;
        }
        String name = getAllColumnNames().get(index);
        if (name == null) {
            return;
        }
        SortInfo info = new SortInfo(name, sortOrder);
        addToSort(info);
    }


    @Deprecated  // use addIndexToSortOn(int index, SortOrder sortOrder)
    public void addIndexToSortOn(int index, boolean ascending) {
        this.addIndexToSortOn(index, (ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING));
    }

    public void setColumnIndicesSortedOn(List<Integer> indices) {
        sortInfo.clear();
        for (Integer index : indices) {
            String name = getAllColumnNames().get(index);
            SortInfo info = new SortInfo(name);
            sortInfo.add(info);
        }
    }

    public void setSortInfo(List<SortInfo> newSortInfo) {
        sortInfo.clear();
        for (SortInfo info : newSortInfo) {
            addToSort(info);
        }
    }

    public List<SortInfo> getSortInfo() {
        return sortInfo;
    }

    public void addToSort(SortInfo info) {
        if (!getAllColumnNames().contains(info.getName())) {
            return;
        }
        sortInfo.add(info);
    }

    public void removeAllFromSort() {
        sortInfo.clear();
    }

    public void removeFromSort(SortInfo info) {
        if (!getAllColumnNames().contains(info.getName())) {
            return;
        }
        sortInfo.remove(info);

    }

    public List<Integer> getColumnsToSortOn() {  // List of indices
        List<Integer> indices = new ArrayList<>();
        for (SortInfo info : sortInfo) {
            if (getAllColumnNames().contains(info.getName())) {
                indices.add(getAllColumnNames().indexOf(info.getName()));
            }
        }
        return indices;
    }
    @Deprecated // Use getSortInfo().get(index).getSortOrder
    public boolean getAscending(int index) {
        if (index >= getAllColumnNames().size()) {
            return true; // default
        }
        String name = getAllColumnNames().get(index);
        for (SortInfo info : sortInfo) {
            if (info.getName().equals(name)) {
                return info.getSortOrder() == SortOrder.ASCENDING;
            }
        }
        return true; // default
    }

    public void clear() {
        sortInfo.clear();
    }

    public int getSortIndex(String name) {
        for (int i = 0; i < sortInfo.size(); i++) {
            if (sortInfo.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSortedOn(int index) {
        return (index < getAllColumnNames().size()) && (-1 != getSortIndex(getAllColumnNames().get(index)));
    }

    public void toggleAscending(int index) {
        if (index >= getAllColumnNames().size()) {
            return;
        }
        int i = getSortIndex(getAllColumnNames().get(index));
        if (i >= sortInfo.size()) {
            return;
        }
        SortInfo info = sortInfo.get(i);
        info.setSortOrder(info.getSortOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING);
    }

    public void updateRowSorter(TableRowSorter<? extends TableModel> sorter){
        if  (sorter != null && !sorter.equals(this.sorter)){
            List<RowSorter.SortKey> keys = new ArrayList<>();
            for (SortInfo sortInfo : this.getSortInfo()){
                int columnIndex = getColumnIndex(sortInfo.getName());
                keys.add(new RowSorter.SortKey(columnIndex, sortInfo.getSortOrder())) ;
            }
            sorter.setMaxSortKeys(Math.max(1,keys.size()));
            sorter.setSortKeys(keys);
        }
    }

    private int getColumnIndex(String name) {
        if (!getAllColumnNames().contains(name)) {
            return -1;
        }
        return getAllColumnNames().indexOf(name);
    }

    private void initColumnNames(){
        this.columnNames.clear();
        this.allColumnNames.clear();

        TableModel model = sorter.getModel();
        for (int i = 0; i < model.getColumnCount(); i++){
            allColumnNames.add(model.getColumnName(i));
            if (TableSortSupport.class.isAssignableFrom(model.getClass())){
               if ( ! ((TableSortSupport) model).isColumnSortable(i)){
                   continue;
               }
            }
            columnNames.add(model.getColumnName(i));
        }
    }

    private void updateSortInfo(){
        this.sortInfo.clear();
        for(RowSorter.SortKey key: sorter.getSortKeys()){
            if (key.getSortOrder()==SortOrder.UNSORTED){
                continue;
            }
            sortInfo.add(new SortInfo(getAllColumnNames().get(key.getColumn()),key.getSortOrder()));
        }
    }

    public List<String> getAllColumnNames() {
        if (allColumnNames.isEmpty()) {
            return getColumnNames();    // Back-up option: if allColumnNames is not used, then use all names mentioned in columnNames list
        }
        return allColumnNames;
    }

    public void setAllColumnNames(List<String> allColumnNames) {
        this.allColumnNames = allColumnNames;
    }
}
