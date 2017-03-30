/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Date', {
    extend: 'Uni.view.search.field.DateTime',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.view.search.field.internal.SingleDateRange',
        'Uni.view.search.field.internal.DateField'
    ],
    alias: 'widget.uni-search-criteria-date',
    text: Uni.I18n.translate('search.field.date.text', 'UNI', 'Date'),

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            width: '455',
            operator: '==',
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-datefield',
                //'!=': 'uni-search-internal-datefield',
                //'>': 'uni-search-internal-datefield',
                //'>=': 'uni-search-internal-datefield',
                //'<': 'uni-search-internal-datefield',
                //'<=': 'uni-search-internal-datefield',
                'BETWEEN': 'uni-search-internal-single-daterange'
            },
            listeners: {
                change: {
                    fn: me.onInputChange,
                    scope: me
                }
            }
        }, config)
    }
});