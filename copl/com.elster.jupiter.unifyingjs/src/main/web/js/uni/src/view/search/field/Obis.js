/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Obis', {
    extend: 'Uni.view.search.field.Simple',
    xtype: 'uni-search-criteria-obis',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.view.search.field.internal.InputObis'
    ],

    init: function () {
        var me = this;

        me.items = {
            xtype: 'uni-search-internal-criterialine',
            operator: '==',
            padding: 5,
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-input-obis'
                //'!=': 'uni-search-internal-input'
            },
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                },
                reset: {
                    fn: me.onInputReset,
                    scope: me
                }
            }
        };
    }
});