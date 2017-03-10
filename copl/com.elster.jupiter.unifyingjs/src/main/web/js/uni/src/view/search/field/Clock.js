/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Clock', {
    extend: 'Uni.view.search.field.DateTime',
    alias: 'widget.uni-search-criteria-clock',
    requires: [
        'Uni.view.search.field.internal.CriteriaLine',
        'Uni.view.search.field.internal.ClockField',
        'Uni.view.search.field.internal.ClockRange'
    ],
    text: Uni.I18n.translate('search.field.clock.text', 'UNI', 'Clock'),

    createCriteriaLine: function(config) {
        var me = this;

        return Ext.apply({
            xtype: 'uni-search-internal-criterialine',
            width: '455',
            operator: '==',
            removable: false,
            operatorMap: {
                '==': 'uni-search-internal-clock',
                //'!=': 'uni-search-internal-clock',
                //'>': 'uni-search-internal-clock',
                //'>=': 'uni-search-internal-clock',
                //'<': 'uni-search-internal-clock',
                //'<=': 'uni-search-internal-clock',
                'BETWEEN': 'uni-search-internal-clockrange'
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