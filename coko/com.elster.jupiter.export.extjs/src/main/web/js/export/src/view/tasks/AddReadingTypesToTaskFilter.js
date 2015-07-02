Ext.define('Dxp.view.tasks.AddReadingTypesToTaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-addreadingtypestotaskfilter',

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
            store: 'Dxp.store.UnitsOfMeasure',
            applyParamValue: function (params, includeUndefined, flattenObjects) {
                var me = this,
                    record = me.findRecord(me.valueField || me.displayField, me.getValue());

                if (record) {
                    params['multiplier'] = record.get('multiplier');
                    params['unitOfMeasure'] = record.get('unit');
                }
            }
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
            valueField: 'name',
            store: 'Dxp.store.Intervals',
            applyParamValue: function (params, includeUndefined, flattenObjects) {
                var me = this,
                    record = me.findRecord(me.valueField || me.displayField, me.getValue());

                if (record) {
                    params['time'] = record.get('time');
                    params['macro'] = record.get('macro');
                }
            }
        },
        {
            type: 'noui',
            dataIndex: 'selectedReadings',
            itemId: 'selectedReadingsFilterComponent'
        }
    ],

    setSelectedReadings: function(selectedReadings) {
        var noUiComponent = this.getFilterByItemId('selectedReadingsFilterComponent');
        noUiComponent.setInitialValue(selectedReadings);
        noUiComponent.setFilterValue(selectedReadings);
    }
});