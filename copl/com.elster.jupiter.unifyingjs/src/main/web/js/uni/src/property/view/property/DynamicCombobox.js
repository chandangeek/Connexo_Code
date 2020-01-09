/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.DynamicCombobox', {
    extend: 'Uni.property.view.property.BaseCombo',

    require: [
        'Uni.property.store.DynamicComboboxData'
    ],

    store: null,
    validComponent: true,

    initComponent: function() {
        var me = this,
            entityType = me.parentForm && me.parentForm.context && me.parentForm.context.id,
            url = me.getProperty().getPropertyType().raw.valueProviderUrl || '',
            re = /\{.*?\}/g;

        me.store = Ext.getStore('Uni.property.store.DynamicComboboxData');

        me.callParent();
        //check if url is empty otherwise mark combo with an error
        if (!Ext.isEmpty(url) && entityType) {
            url = url.replace(re, entityType);
            me.store.getProxy().setUrl(url);
        } else{
            me.validComponent = false;
        }
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            minChars: 0,
            displayField: 'name',
            valueField: 'name',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            listeners: {
                afterrender: function(combo) {
                    //check if store is empty otherwise we trying to load it on each combo expand
                    var combo = this;
                    if (me.validComponent){
                        var propsStore = Ext.getStore('Uni.property.store.DynamicComboboxData') || Ext.create('Uni.property.store.DynamicComboboxData');
                        propsStore.load(function(records, operation, success) {
                             if (propsStore.getPropertiesData()) combo.bindStore(propsStore.getPropertiesData());
                             success ? combo.clearInvalid() : combo.markInvalid(Uni.I18n.translate('general.dynamicComboError', 'UNI', 'There is an error downloading data from server'));
                        });
                    }else{
                        combo.markInvalid(Uni.I18n.translate('general.dynamicComboError', 'UNI', 'There is an error downloading data from server'));
                    }
                },
                change: function(combo){
                    combo.clearInvalid();
                }
            }
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
