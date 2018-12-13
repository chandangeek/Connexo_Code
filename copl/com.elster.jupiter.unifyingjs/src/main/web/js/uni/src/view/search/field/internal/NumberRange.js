/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.NumberRange', {
    extend: 'Ext.form.FieldSet',
    xtype: 'uni-search-internal-numberrange',
    requires: [
        'Uni.view.search.field.internal.NumberField'
    ],
    width: '455',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        margin: '0 0 5 0'
    },
    margin: 0,
    padding: 0,
    border: false,
    itemsDefaultConfig: {
        allowBlank: false
    },

    setValue: function(value) {
        this.items.each(function(item, index) {
            item.setValue(value[index]);
        });
    },

    isValid: function() {
        var me = this,
            fromField = me.down('#from'),
            toField = me.down('#to'),
            isFromValid,
            isToValid;

        if (me.isFilterField && fromField.getValue()) {
            if (fromField.getValue() > toField.getValue() || fromField.getValue() == toField.getValue()) {
                toField.validate();
                return false;
            }
        }

        isFromValid = fromField.isValid();
        isToValid = toField.isValid();

        return isFromValid && isToValid;
    },

    onChange: function () {
        if (this.validateOnChange) {
            this.down('#from').validate();
            this.down('#to').validate();
        }

        this.fireEvent('change', this, this.getValue());
    },

    getValue: function() {
        var value = [];
        this.items.each(function(item){
            if (!Ext.isEmpty(item.getValue())) {value.push(item.getValue());}
        });
        return Ext.isEmpty(value) ? null : value;
    },

    reset: function() {
        this.items.each(function(item){
            item.reset();
        });
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change"
        );

        me.items = [
            {
                xtype: 'uni-search-internal-numberfield',
                itemId: 'from',
                validateOnChange: me.validateOnChange,
                itemsDefaultConfig: me.itemsDefaultConfig,
                listeners: {
                    change: function (field, newValue) {
                        var toFieldMinValue = me.isFilterField ? newValue + 1 : newValue;
                        me.down('#to').getField().setMinValue(!Ext.isEmpty(newValue) ? toFieldMinValue : 0);
                        me.onChange();
                    }
                }
            },
            {
                xtype: 'uni-search-internal-numberfield',
                itemId: 'to',
                validateOnChange: me.validateOnChange,
                itemsDefaultConfig: me.itemsDefaultConfig,
                listeners: {
                    change: function (field, newValue) {
                        var fromFieldMaxValue = me.isFilterField ? newValue - 1 : newValue;
                        me.down('#from').getField().setMaxValue(!Ext.isEmpty(newValue) ? fromFieldMaxValue : Number.MAX_SAFE_INTEGER);
                        me.onChange();
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});