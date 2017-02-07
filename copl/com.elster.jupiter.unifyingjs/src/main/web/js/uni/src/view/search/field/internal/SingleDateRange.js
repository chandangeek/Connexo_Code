/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.SingleDateRange', {
    extend: 'Uni.view.search.field.internal.DateRange',
    alias: 'widget.uni-search-internal-single-daterange',
    requires: [
        'Uni.view.search.field.internal.DateField'
    ],
    defaults: {
        margin: '0 0 5 0',
        width: 180
    },

    createCriteriaLine: function () {
        var me = this;

        return [
            {
                xtype: 'uni-search-internal-datefield',
                itemId: 'from',
                listeners: {
                    change: function(field) {
                        me.down('#to').setMinValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#to').setMinValue(null);
                    }
                }
            },
            {
                xtype: 'uni-search-internal-datefield',
                itemId: 'to',
                listeners: {
                    change: function(field) {
                        me.down('#from').setMaxValue(new Date(field.getValue()));
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#from').setMaxValue(null);
                    }
                }
            }
        ]
    }
});