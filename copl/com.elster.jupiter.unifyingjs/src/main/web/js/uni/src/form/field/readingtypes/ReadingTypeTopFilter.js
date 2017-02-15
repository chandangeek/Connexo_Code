/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.ReadingTypeTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'uni-add-reading-type-top-filter',
    historyEnabled: false,

    selectedReadingTypes: null,
    isEquidistant: false,
    isActive: false,

    initComponent: function () {
        var me = this;

        this.filters = [
            {
                type: 'text',
                dataIndex: 'fullAliasName',
                itemId: 'name-field',
                emptyText: Uni.I18n.translate('readingTypesField.filter.readingTypeName', 'UNI', 'Reading type name')
            },
            {
                type: 'combobox',
                dataIndex: 'unit',
                itemId: 'unit-of-measure-combo',
                emptyText: Uni.I18n.translate('general.unitOfMeasure', 'UNI', 'Unit of measure'),
                store: 'Uni.store.UnitsOfMeasure',
                displayField: 'name',
                valueField: 'name',
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                        value = me.getValue();
                    var record = me.findRecord(me.valueField || me.displayField, value);
                    if (record) {
                        params['multiplier'] = record.get('multiplier');
                        params['unit'] = record.get('unit');
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'timeOfUse',
                itemId: 'time-of-use-field',
                emptyText: Uni.I18n.translate('general.timeOfUse', 'UNI', 'Time of use'),
                store: 'Uni.store.TimeOfUse',
                displayField: 'name',
                valueField: 'tou'
            },
            {
                type: 'combobox',
                dataIndex: 'measurementPeriod',
                itemId: 'interval-field',
                emptyText: Uni.I18n.translate('general.interval', 'UNI', 'Interval'),
                store: 'Uni.store.Intervals',
                displayField: 'name',
                valueField: 'name',
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                        value = me.getValue();
                    var record = me.findRecord(me.valueField || me.displayField, value);
                    if (record) {
                        params['measurementPeriod'] = record.get('time');
                        params['macroPeriod'] = record.get('macro');
                    }
                }
            },
            {
                type: 'noui',
                itemId: 'selectedReadingsFilter',
                dataIndex: 'selectedreadingtypes'
            },
            {
                type: 'noui',
                itemId: 'equidistantFilter',
                dataIndex: 'equidistant'
            },
            {
                type: 'noui',
                itemId: 'activeFilter',
                dataIndex: 'active'
            }
        ];

        me.callParent(arguments);
        if (Ext.isArray(me.selectedReadingTypes) && me.selectedReadingTypes.length) {
            me.setSelectedReadings(me.selectedReadingTypes);
        }
        if (me.isEquidistant) {
            me.setEquidistant();
        }
        if (me.isActive) {
            me.setActive();
        }
    },

    setSelectedReadings: function (readingArray) {
        this.getFilterByItemId('selectedReadingsFilter').setInitialValue(readingArray);
        this.getFilterByItemId('selectedReadingsFilter').setFilterValue(readingArray);
    },

    setEquidistant: function () {
        this.getFilterByItemId('equidistantFilter').setInitialValue(true);
        this.getFilterByItemId('equidistantFilter').setFilterValue(true);
    },

    setActive: function () {
        this.getFilterByItemId('activeFilter').setInitialValue(true);
        this.getFilterByItemId('activeFilter').setFilterValue(true);
    }
});