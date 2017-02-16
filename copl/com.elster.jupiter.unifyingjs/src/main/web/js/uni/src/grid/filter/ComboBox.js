/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.ComboBox
 */
Ext.define('Uni.grid.filter.ComboBox', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'uni-grid-filter-combobox',

    mixins: [
        'Uni.grid.filter.Base'
    ],

    fieldLabel: Uni.I18n.translate('grid.filter.combobox.label', 'UNI', 'Combobox'),

    queryMode: 'local',
    valueField: 'value',
    displayField: 'display',
    forceSelection: true,

    initComponent: function () {
        var me = this;

        if (Ext.isDefined(me.options) && !Ext.isDefined(me.store)) {
            me.store = me.createStoreFromOptions();
        }

        me.callParent(arguments);

        me.on('specialkey', function (field, event) {
            if (event.getKey() === event.ENTER) {
                me.assertValue();
                me.fireFilterUpdateEvent();
            }
        }, me);
    },

    createStoreFromOptions: function () {
        var me = this,
            options = me.options,
            store;

        store = Ext.create('Ext.data.Store', {
            fields: ['value', 'display'],
            data: options
        });

        return store;
    }
});