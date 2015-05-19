Ext.define('Dxp.view.tasks.AddReadingTypesToTaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-addreadingtypestotaskfilter',

    // TODO Use the filter.
    store: 'Dxp.store.LoadedReadingTypes',

    filters: [
        {
            type: 'text',
            dataIndex: 'name',
            emptyText: Uni.I18n.translate('dataExportTasks.readingTypeName', 'DES', 'Reading type name'),
            displayField: 'name',
            valueField: 'id'
        },
        {
            type: 'combobox',
            dataIndex: 'unitOfMeasure',
            emptyText: Uni.I18n.translate('dataExportTasks.unitOfMeasure', 'DES', 'Unit of measure'),
            displayField: 'name',
            valueField: 'name',
            store: 'Dxp.store.UnitsOfMeasure'
        },
        {
            type: 'combobox',
            dataIndex: 'tou',
            emptyText: Uni.I18n.translate('dataExportTasks.timeOfUse', 'DES', 'Time of use'),
            displayField: 'name',
            valueField: 'tou',
            store: 'Dxp.store.TimeOfUse'
        },
        {
            type: 'combobox',
            dataIndex: 'time',
            emptyText: Uni.I18n.translate('dataExportTasks.interval', 'DES', 'Interval'),
            displayField: 'name',
            valueField: 'time',
            store: 'Dxp.store.Intervals'
        }
    ]
});