/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.AddReadingTypesToTaskFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-addreadingtypestotaskfilter',

    store: 'Dxp.store.LoadedReadingTypes',
    defaultFilters: null,

    initComponent: function () {
        var me = this,
            selectedReadingTypesFilter,
            activeFilter;

        if (!Ext.isEmpty(me.defaultFilters)) {
            me.hasDefaultFilters = true;
            selectedReadingTypesFilter = me.defaultFilters.selectedreadingtypes
                ? me.defaultFilters.selectedreadingtypes
                : null;
            activeFilter = me.defaultFilters.active
                ? me.defaultFilters.active
                : null
        }

        me.filters = [
            {
                type: 'text',
                dataIndex: 'fullAliasName',
                emptyText: Uni.I18n.translate('dataExportTasks.readingTypeName', 'DES', 'Reading type name'),
                displayField: 'name',
                valueField: 'id',
                itemId: 'txt-reading-type'
            },
            {
                type: 'combobox',
                dataIndex: 'unit',
                emptyText: Uni.I18n.translate('dataExportTasks.unitOfMeasure', 'DES', 'Unit of measure'),
                displayField: 'name',
                valueField: 'name',
                itemId: 'cbo-unit-of-measure',
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
                dataIndex: 'timeOfUse',
                emptyText: Uni.I18n.translate('dataExportTasks.timeOfUse', 'DES', 'Time of use'),
                displayField: 'name',
                itemId: 'cbo-time-of-use',
                valueField: 'tou',
                store: 'Dxp.store.TimeOfUse'
            },
            {
                type: 'combobox',
                dataIndex: 'measurementPeriod',
                emptyText: Uni.I18n.translate('dataExportTasks.interval', 'DES', 'Interval'),
                displayField: 'name',
                valueField: 'name',
                itemId: 'cbo-interval',
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
                type: 'combobox',
                dataIndex: 'metrologyConfiguration',
                emptyText: Uni.I18n.translate('dataExportTasks.metrologyConfiguration', 'DES', 'Metrology configuration'),
                displayField: 'name',
                itemId: 'cbo-metrology-configuration',
                valueField: 'id',
                store: 'Dxp.store.MetrologyConfigurations',
                privileges: Uni.util.Application.getAppName() === 'MdmApp'
            },
            {
                type: 'combobox',
                dataIndex: 'metrologyPurpose',
                emptyText: Uni.I18n.translate('dataExportTasks.purpose', 'DES', 'Purpose'),
                displayField: 'name',
                itemId: 'cbo-purpose',
                valueField: 'id',
                store: 'Dxp.store.MetrologyPurposes',
                privileges: Uni.util.Application.getAppName() === 'MdmApp'
            },
            {
                type: 'noui',
                dataIndex: 'selectedreadingtypes',
                itemId: 'selectedReadingsFilterComponent',
                initialValue: selectedReadingTypesFilter,
                value: selectedReadingTypesFilter
            },
            {
                type: 'noui',
                itemId: 'activeFilter',
                dataIndex: 'active',
                initialValue: activeFilter,
                value: activeFilter
            }
        ];

        me.callParent(arguments);
    }
});