/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.DynamicCombobox', {
    extend: 'Uni.property.view.property.BaseCombo',

    require: [
        'Uni.property.store.DynamicComboboxData'
    ],

    store: null,

    initComponent: function() {
        var me = this,
            entityTypeName = me.parentForm.entityTypeName,
            url =  me.getProperty().getPropertyType().get('simplePropertyType');

        me.store = Ext.getStore('Uni.property.store.DynamicComboboxData');
        me.callParent();
        me.store.getProxy().setUrl(url);
        me.store.getProxy().setExtraParam('deviceId', entityTypeName);
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.store,
            minChars: 0,
            displayField: 'name',
            valueField: 'name',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
        }
    },

    getField: function () {
        return this.down('combobox');
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox') && this.down('combobox').clearInvalid();
    }
});
