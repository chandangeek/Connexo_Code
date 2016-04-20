Ext.define('Uni.view.search.field.Quantity', {
    extend: 'Uni.view.search.field.Numeric',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine'
    ],
    alias: 'widget.uni-search-criteria-quantity',
    minWidth: 400,

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