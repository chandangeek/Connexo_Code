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
            entityType = me.parentForm.context.id,
            url = me.getProperty().getPropertyType().raw.propertyValueInfo.defaultValue || '',
            re = /\{.*?\}/g;

        me.store = Ext.getStore('Uni.property.store.DynamicComboboxData');

        me.callParent();

        if (!Ext.isEmpty(url)) {
            url = url.replace(re, entityType);
            me.store.getProxy().setUrl(url);
            me.store.load();
        } else {
            this.markInvalid( Uni.I18n.translate('general.dynamicComboError', 'UNI', 'There is an error downloading data from server'));

        }

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
            listeners: {
                expand: function(combo) {
                    var me = this;

                    combo.getStore().load(function(records, operation, success) {
                           success ? me.clearInvalid() : me.markInvalid();
                        }
                    );
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
