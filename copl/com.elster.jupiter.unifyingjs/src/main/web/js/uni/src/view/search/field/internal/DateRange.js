/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.DateRange', {
    extend: 'Ext.form.FieldSet',
    xtype: 'uni-search-internal-daterange',
    requires: [
        'Uni.view.search.field.internal.DateTimeField'
    ],
    layout: 'vbox',
    margin: 0,
    padding: 0,
    defaults: {
        margin: '0 0 5 0'
    },
    border: false,

    setValue: function(value) {
        this.items.each(function(item, index) {
            item.setValue(value[index]);
        });
    },

    getValue: function() {
        var value = [];
        this.items.each(function(item){
            if (!Ext.isEmpty(item.getValue())) {
                value.push(item.getValue());
            }
        });
        return Ext.isEmpty(value) ? null : value;
    },

    reset: function() {
        this.items.each(function(item){
            item.reset();
        });
    },

    createCriteriaLine: function () {
        var me = this;

        return [
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'from',
                listeners: {
                    change: function (field, newValue) {
                        me.down('#to').setMinValue(!Ext.isEmpty(newValue) ? new Date(newValue) : null);
                        me.fireEvent('change', me, me.getValue());
                    }
                }
            },
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'to',
                listeners: {
                    change: function (field, newValue) {
                        me.down('#from').setMaxValue(!Ext.isEmpty(newValue) ? new Date(newValue) : null);
                        me.fireEvent('change', me, me.getValue());
                    }
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change"
        );

        me.items = me.createCriteriaLine();

        me.callParent(arguments);
    }
});