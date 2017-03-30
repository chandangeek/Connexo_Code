/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.Checkbox
 */
Ext.define('Uni.grid.filter.Checkbox', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-grid-filter-checkbox',

    mixins: [
        'Uni.grid.filter.Base'
    ],

    fieldLabel: Uni.I18n.translate('grid.filter.checkbox.label', 'UNI', 'Checkbox'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    groupName: undefined,
    valueField: 'value',
    displayField: 'display',
    defaultType: 'checkboxfield',

    initComponent: function () {
        var me = this;

        if (!Ext.isDefined(me.groupName)) {
            me.groupName = me.generateRandomName();
        }

        if (Ext.isDefined(me.options) && !Ext.isDefined(me.items)) {
            me.items = me.createItemsFromOptions();
        }

        me.callParent(arguments);
    },

    createItemsFromOptions: function () {
        var me = this,
            options = me.options,
            items = [];

        Ext.Array.each(options, function (option) {
            Ext.applyIf(option, {
                name: me.groupName,
                boxLabel: option[me.displayField],
                inputValue: option[me.valueField]
            });

            delete option[me.displayField];
            delete option[me.valueField];

            items.push(option);
        }, me);

        return items;
    },

    setFilterValue: function (data) {
        var me = this,
            values = {};

        values[me.groupName] = data;
        me.setValues(values);
    },

    getParamValue: function () {
        return this.getValues()[this.groupName];
    },

    resetValue: function () {
        var me = this;

        me.items.each(function (field) {
            field.reset();
        }, me);
    }
});