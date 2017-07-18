/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.TimeOfDayField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.uni-search-internal-timeOfDayField',
    layout: 'hbox',
    requires: [
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value'
    ],
    defaults: {
        margin: '0 10 0 0'
    },
    removable: false,

    getValue: function() {
        var hoursValue = this.down('#hours').getValue(),
            minutesValue = this.down('#minutes').getValue(),
            value = null;

        if (hoursValue || minutesValue) {
            value = 0;
            if (hoursValue) {
                value += hoursValue * 3600;
            }
            if (minutesValue) {
                value += minutesValue * 60;
            }
        }

        return value;
    },

    setValue: function (value) {
        this.down('#hours').setValue(parseInt(value/3600));
        this.down('#minutes').setValue(value%3600/60);
    },

    setMinValue: function (minValue) {
        this.down('#hours').setMinValue(parseInt(minValue/3600));
        this.down('#minutes').setMinValue(minValue%3600/60);
    },

    setMaxValue: function (maxValue) {
        this.down('#hours').setMaxValue(parseInt(maxValue/3600));
        this.down('#minutes').setMaxValue(maxValue%3600/60);
    },

    reset: function() {
        this.down('#hours').reset();
        this.down('#minutes').reset();
        this.fireEvent('reset');
    },

    onChange: function() {
        var value = this.getValue();

        this.fireEvent('change', this, value);
    },

    onRemove: Ext.emptyFn,

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            {
                xtype: 'numberfield',
                itemId: 'hours',
                value: 0,
                maxValue: 23,
                minValue: 0,
                width: 55,
                listeners: {
                    change: me.onChange,
                    scope: me
                }
            },
            {
                xtype: 'numberfield',
                itemId: 'minutes',
                value: 0,
                maxValue: 59,
                minValue: 0,
                width: 55,
                listeners: {
                    change: me.onChange,
                    scope: me
                }

            }
        ];

        me.callParent(arguments);
    }
});