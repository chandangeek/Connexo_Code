Ext.define('Est.main.view.ReadingTypeTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'est-main-view-readingtypetopfilter',
    requires: [
        'Est.main.store.ReadingTypes',
        'Est.main.store.UnitsOfMeasure',
        'Est.main.store.TimeOfUse',
        'Est.main.store.Intervals'
    ],
    store: 'Est.main.store.ReadingTypes',
    filterDefault: {},

    initComponent: function() {
        var me = this;

        this.filters = [
            {
                type: 'text',
                dataIndex: 'name',
                itemId: 'name-field',
                emptyText: Uni.I18n.translate('general.readingTypeName', 'EST', 'Reading type name'),
            },
            {
                type: 'combobox',
                dataIndex: 'unitOfMeasure',
                itemId: 'unitOfMeasureCombo',
                emptyText: Uni.I18n.translate('general.unitOfMeasure', 'EST', 'Unit of measure'),
                store: 'Est.main.store.UnitsOfMeasure',
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
                itemId: 'time-of-use-field',
                emptyText: Uni.I18n.translate('general.timeOfUse', 'EST', 'Time of use'),
                store: 'Est.main.store.TimeOfUse',
                displayField: 'name',
                valueField: 'tou'


            },
            {
                type: 'combobox',
                dataIndex: 'time',
                itemId: 'interval-field',
                emptyText: Uni.I18n.translate('general.interval', 'EST', 'Interval'),
                store: 'Est.main.store.Intervals',
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
            },
            {
                type: 'noui',
                itemId: 'equidistantFilter',
                dataIndex: 'equidistant'
            }
        ];

        me.callParent(arguments);
    },

    setSelectedReadings: function(readingArray){
        this.getFilterByItemId('selectedReadingsFilter').setInitialValue(readingArray);
        this.getFilterByItemId('selectedReadingsFilter').setFilterValue(readingArray);
        this.getFilterByItemId('equidistantFilter').setInitialValue('true');
        this.getFilterByItemId('equidistantFilter').setFilterValue('true');
    }
});