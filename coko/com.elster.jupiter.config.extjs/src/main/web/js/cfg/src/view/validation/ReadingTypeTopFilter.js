/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.ReadingTypeTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.readingTypesToAddForRule',
    itemId: 'reading-types-to-add-for-rule',
    requires: [
        'Cfg.store.ReadingTypesToAddForRule',
        'Cfg.store.UnitsOfMeasure',
        'Cfg.store.TimeOfUse',
        'Cfg.store.Intervals'
    ],
    store: 'Cfg.store.ReadingTypesToAddForRule',
    filterDefault: {},

    initComponent: function() {
        var me = this;

        this.filters = [
            {
                type: 'text',
                dataIndex: 'fullAliasName',
                itemId: 'readingTypeNameCombo',
                emptyText: Uni.I18n.translate('validation.readingTypeName', 'CFG', 'Reading type name')
            },
            {
                type: 'combobox',
                dataIndex: 'unit',
                itemId: 'unitOfMeasureCombo',
                emptyText: Uni.I18n.translate('validation.unitOfMeasure', 'CFG', 'Unit of measure'),
                store:  'UnitsOfMeasure',
                displayField: 'name',
                valueField: 'name',
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                    value = me.getValue();
                    var record = me.findRecord(me.valueField || me.displayField, value);
                    if(record){
                        params['multiplier'] = record.get('multiplier');
                        params['unit'] = record.get('unit');
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'timeOfUse',
                emptyText: Uni.I18n.translate('validation.timeOfUse', 'CFG', 'Time of use'),
                itemId: 'timeOfUseCombo',
                store: 'TimeOfUse',
                displayField: 'name',
                valueField: 'tou'


            },
            {
                type: 'combobox',
                dataIndex: 'measurementPeriod',
                emptyText: Uni.I18n.translate('validation.interval', 'CFG', 'Interval'),
                itemId: 'intervalCombo',
                store: 'Intervals',
                displayField: 'name',
                valueField: 'name',
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                        value = me.getValue();
                    var record = me.findRecord(me.valueField || me.displayField, value);
                    if(record){
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
                itemId: 'activeFilter',
                dataIndex: 'active'
            }
        ];

        me.callParent(arguments);
    },

    setSelectedReadings: function(readingArray){
        this.getFilterByItemId('selectedReadingsFilter').setInitialValue(readingArray);
        this.getFilterByItemId('selectedReadingsFilter').setFilterValue(readingArray);
    },

    setActive: function() {
        this.getFilterByItemId('activeFilter').setInitialValue(true);
        this.getFilterByItemId('activeFilter').setFilterValue(true);
    }
});