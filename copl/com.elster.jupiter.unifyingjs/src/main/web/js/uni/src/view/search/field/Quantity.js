Ext.define('Uni.view.search.field.Quantity', {
    extend: 'Uni.view.search.field.Numeric',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],
    alias: 'widget.uni-search-criteria-quantity',
    minWidth: 400,

    //setValue: function (value) {
    //    var me = this,
    //        timeUnit = null;
    //
    //    Ext.Array.each(value, function (record) {
    //        var values = [];
    //
    //        Ext.Array.each(record.get('criteria'), function (criteriaValue) {
    //            var valueToArr = criteriaValue.split(':');
    //
    //            values.push(valueToArr[0]);
    //            timeUnit = ['0', valueToArr[1], valueToArr[2]].join(':');
    //        });
    //
    //        record.set('criteria', values);
    //    });
    //
    //    me.getUnitField().setValue(timeUnit);
    //    me.callParent(arguments);
    //},
    //
    //getValue: function () {
    //    var me = this,
    //        value = this.callParent(arguments);
    //
    //    return value ? value.map(function (v) {
    //        var criteria = v.get('criteria'),
    //            unitFieldValue = me.getUnitField().getValue(),
    //            unit;
    //
    //        if (Ext.isString(unitFieldValue)) {
    //            unit = unitFieldValue.split(':');
    //
    //            if (unit.length === 3) {
    //                v.set('criteria', _.map(Ext.isArray(criteria) ? criteria : [criteria], function (item) {
    //                    return [item, unit[1], unit[2]].join(':');
    //                }));
    //            }
    //        }
    //        return v;
    //    }) : null;
    //},

    createCriteriaLine: function(config) {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                fields: ['id', 'displayValue']
            }),
            defaultUnit;

        store.loadData(me.property.get('values'));

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            itemsDefaultConfig: Ext.apply(me.itemsDefaultConfig, {unitsStore: store}),
            width: '455',
            operator: '==',
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-quantityfield',
                //'!=': 'uni-search-internal-quantityfield',
                //'>': 'uni-search-internal-quantityfield',
                //'>=': 'uni-search-internal-quantityfield',
                //'<': 'uni-search-internal-quantityfield',
                //'<=': 'uni-search-internal-quantityfield',
                'BETWEEN': 'uni-search-internal-quantityrange'
            },
            listeners: {
                change: {
                    fn: me.onValueChange,
                    scope: me
                }
            }
        }, config)
    }
});