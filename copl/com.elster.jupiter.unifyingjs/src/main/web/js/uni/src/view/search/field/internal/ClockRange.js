/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.ClockRange', {
    extend: 'Uni.view.search.field.internal.DateRange',
    alias: 'widget.uni-search-internal-clockrange',
    requires: [
        'Uni.view.search.field.internal.ClockField'
    ],

    createCriteriaLine: function () {
        var me = this;

        return [
            {
                xtype: 'uni-search-internal-clock',
                itemId: 'from',
                listeners: {
                    change: function(field) {
                        me.down('#to datefield').setMinValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#to datefield').setMinValue(null);
                    }
                }
            },
            {
                xtype: 'uni-search-internal-clock',
                itemId: 'to',
                listeners: {
                    change: function(field) {
                        me.down('#from datefield').setMaxValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#from datefield').setMaxValue(null);
                    }
                }
            }
        ]
    }
});