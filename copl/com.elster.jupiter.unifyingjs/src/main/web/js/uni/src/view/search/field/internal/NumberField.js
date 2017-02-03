/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.NumberField', {
    //TODO: replace with Ext.form.field.Number
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-numberfield',
    width: '455',
    layout: 'fit',
    itemsDefaultConfig: {},

    getField: function() {
        return this.down('#filter-input');
    },

    isValid: function() {
        return this.getField().isValid();
    },

    validate: function() {
        return this.getField().validate();
    },

    setValue: function(value) {
        this.getField().setValue(value);
    },

    getValue: function() {
        return this.getField().getValue();
    },

    reset: function() {
        this.getField().reset();
        this.fireEvent('reset', this);
    },

    onChange: function() {
        if (this.validateOnChange) {
            this.getField().validate();
        }

        this.fireEvent('change', this, this.getValue());
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'filter-input',
                width: 180,
                maxValue: Number.MAX_SAFE_INTEGER,
                minValue: 0,
                maxLength: 15,
                allowBlank: true,
                allowExponential: false,
                enforceMaxLength: true,
                validateOnBlur: false,
                margin: '0 5 0 0',
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }, me.itemsDefaultConfig)
        ];

        me.callParent(arguments);
    }
});