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
        var me = this,
            toValue,
            fromValue,
            fromDate,
            toDate;

        return [
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'from',
                listeners: {
                    change: function (field, newValue) {
                        fromValue = newValue;
                        fromDate = new Date(fromValue);
                        if((!Ext.isEmpty(toValue))) {
                            toDate = new Date(toValue);
                            if (fromDate.getDay() == toDate.getDay() &&
                                fromDate.getMonth() == toDate.getMonth() &&
                                fromDate.getYear() == toDate.getYear()) {
                                me.down('#to').setMinValue(fromDate);
                            }
                        }else{
                            if(fromDate != null){
                                me.down('#to').setMinValue(fromDate.setHours(0,0,0,0));
                            }
                            if(toDate != null){
                                me.down('#from').setMaxValue(toDate.setHours(23,59,59,59));
                            }
                        }
                        me.fireEvent('change', me, me.getValue());
                    }
                }
            },
            {
                xtype: 'uni-search-internal-datetimefield',
                itemId: 'to',
                listeners: {
                    change: function (field, newValue) {
                        toValue = newValue;
                        toDate = new Date(toValue);
                        if(!Ext.isEmpty(fromValue)) {
                            fromDate = new Date(fromValue);
                            if (fromDate.getDay() == toDate.getDay() &&
                                fromDate.getMonth() == toDate.getMonth() &&
                                fromDate.getYear() == toDate.getYear()) {
                                me.down('#from').setMaxValue(toDate);

                            }else{
                                if(fromDate != null){
                                    me.down('#to').setMinValue(fromDate.setHours(0,0,0,0));
                                }
                                if(toDate != null){
                                    me.down('#from').setMaxValue(toDate.setHours(23,59,59,59));
                                }

                            }
                        }
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