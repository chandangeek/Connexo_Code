/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Simple', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-simple',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.view.search.field.internal.Textarea'
    ],

    //reset: function() {
    //    this.down('uni-search-internal-criterialine').reset();
    //    this.callParent(arguments);
    //},

    //onInputChange: function() {
    //    this.setValue(this.down('uni-search-internal-criterialine').getValue());
    //},

    initComponent: function () {
        var me = this;

        me.init();
        me.callParent(arguments);
    },

    init: function () {
        var me = this;
        var operators = me.property.get('availableOperators');
        var operatorMap = {
            '==': 'uni-search-internal-input',
            '!=': 'uni-search-internal-input',
        }

        if (Ext.Array.contains(operators, 'IN')) {
            operatorMap['IN'] = 'uni-search-internal-textarea'
        }

        me.items = {
            xtype: 'uni-search-internal-criterialine',
            operator: '==',
            padding: 5,
            removable: false,
            operatorMap: operatorMap,
            listeners: {
                change: {
                    fn: me.onValueChange,
                    scope: me
                }
            },
        };
    }
});