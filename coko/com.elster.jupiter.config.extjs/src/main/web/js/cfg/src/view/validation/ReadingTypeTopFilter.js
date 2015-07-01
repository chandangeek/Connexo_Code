Ext.define('Cfg.view.validation.ReadingTypeTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.readingTypesToAddForRule',
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
                dataIndex: 'name',
                emptyText: Uni.I18n.translate('validation.readingTypeName', 'CFG', 'Reading type name'),
            },
            {
                type: 'combobox',
                dataIndex: 'unitOfMeasure',
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
                        params['unitOfMeasure'] = record.get('unit');
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'tou',
                emptyText: Uni.I18n.translate('validation.timeOfUse', 'CFG', 'Time of use'),
                store: 'TimeOfUse',
                displayField: 'name',
                valueField: 'tou'


            },
            {
                type: 'combobox',
                dataIndex: 'time',
                emptyText: Uni.I18n.translate('validation.interval', 'CFG', 'Interval'),
                store: 'Intervals',
                displayField: 'name',
                valueField: 'name',
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                        value = me.getValue();
                    var record = me.findRecord(me.valueField || me.displayField, value);
                    if(record){
                        params['time'] = record.get('time');
                        params['macro'] = record.get('macro');
                    }
                }
            },
            {
                type: 'noui',
                itemId: 'selectedReadingsFilter',
                dataIndex: 'selectedReadings'
            }
        ];

        me.callParent(arguments);
    },

    setSelectedReadings: function(readingArray){
        this.getFilterByItemId('selectedReadingsFilter').setFilterValue(readingArray);
    }
});