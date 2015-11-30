Ext.define('Dxp.view.tasks.AddReadingTypesToTaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-addreadingtypestotaskfilter',

    store: 'Dxp.store.LoadedReadingTypes',

    filters: [
        {
            type: 'text',
            dataIndex: 'fullAliasName',
            emptyText: Uni.I18n.translate('dataExportTasks.readingTypeName', 'DES', 'Reading type name'),
            displayField: 'name',
            valueField: 'id'
        },
        {
            type: 'combobox',
            dataIndex: 'unit',
            emptyText: Uni.I18n.translate('dataExportTasks.unitOfMeasure', 'DES', 'Unit of measure'),
            displayField: 'name',
            valueField: 'name',
            store: 'Dxp.store.UnitsOfMeasure',
            applyParamValue: function (params, includeUndefined, flattenObjects) {
                var me = this,
                    record = me.findRecord(me.valueField || me.displayField, me.getValue());

                if (record) {
                    params['multiplier'] = record.get('multiplier');
                    params['unit'] = record.get('unit');
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
            dataIndex: 'measurementPeriod',
            emptyText: Uni.I18n.translate('dataExportTasks.interval', 'DES', 'Interval'),
            displayField: 'name',
            valueField: 'name',
            store: 'Dxp.store.Intervals',
            applyParamValue: function (params, includeUndefined, flattenObjects) {
                var me = this,
                    record = me.findRecord(me.valueField || me.displayField, me.getValue());

                if (record) {
                    params['measurementPeriod'] = record.get('time');
                    params['macroPeriod'] = record.get('macro');
                }
            }
        },
        {
            type: 'noui',
            dataIndex: 'selectedreadingtypes',
            itemId: 'selectedReadingsFilterComponent'
        },
        {
            type: 'noui',
            itemId: 'activeFilter',
            dataIndex: 'active'
        }
    ],

    setSelectedReadings: function(selectedReadings) {
        var noUiComponent = this.getFilterByItemId('selectedReadingsFilterComponent');
        noUiComponent.setInitialValue(selectedReadings);
        noUiComponent.setFilterValue(selectedReadings);
        this.getFilterByItemId('activeFilter').setInitialValue(true);
        this.getFilterByItemId('activeFilter').setFilterValue(true);
    }
});