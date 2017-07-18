/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                fields: ['id', 'displayValue'],
                data: me.property.get('values')
            });

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            itemsDefaultConfig: Ext.apply({unitsStore: store}, me.itemsDefaultConfig),
            width: '455',
            operator: 'BETWEEN',
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
                },
                reset: {
                    fn: me.onReset,
                    scope: me
                }
            }
        }, config)
    },

    onValueChange: function () {
        var me = this,
            value = me.getValue(),
            clearBtn = me.down('#clearall');

        me.callParent(arguments);

        if (clearBtn) {
            if (!Ext.isEmpty(value)) {
                clearBtn.setDisabled(false);
            } else {
                clearBtn.setDisabled(me.isUnitChanged());
            }
        }
    },

    isUnitChanged: function () {
        var me = this,
            result = false;

        Ext.Array.each(me.query('#unit-combo'), function (unitCombo) {
            if (unitCombo.value === unitCombo.originalValue) {
                result = true;
            }
        });

        return result;
    }
});