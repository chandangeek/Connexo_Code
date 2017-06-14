/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Reference', {
    extend: 'Uni.property.view.property.BaseCombo',

    getEditCmp: function () {
        var me = this;

        me.referencesStore = Ext.create('Ext.data.Store', {
            fields: ['key', 'value'],
            storeId: me.key + 'store'
        });
        // clear store
        _.forEach(me.getProperty().getPossibleValues(), function (item) {
            me.referencesStore.add({key: item.id, value: item.name});
        });

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.referencesStore,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank,
            blankText: me.blankText
        }
    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            renderer: function (value) {
                var result,
                    valueIsObject = Ext.isObject(value);
                if (value) {
                    result = Ext.Array.findBy(me.getProperty().getPossibleValues(), function (item) {
                        return valueIsObject ? value.id === item.id : value === item.id;
                    });
                    result = Ext.isObject(result) ? result.name : Ext.String.htmlEncode(value);
                }

                return result || (Ext.isEmpty(me.emptyText) ? '-' : me.emptyText);
            }
        }
    },

    getValue: function () {
        return this.getField().getValue();
    },

    getField: function () {
        return this.down('combobox');
    },

    /**
     * Sets value to the view component
     * Override this method if you have custom logic of value transformation
     * @see Uni.property.view.property.Time for example
     *
     * @param value
     */
    setValue: function (value) {
        if (this.isEdit) {
            if (this.getProperty().get('hasValue') && !this.userHasViewPrivilege && this.userHasEditPrivilege) {
                this.getField().emptyText = Uni.I18n.translate('general.valueProvided', 'UNI', 'Value provided - no rights to see the value.');
            } else {
                this.getField().emptyText = '';
            }
            this.getField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.id : value : null);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.name : value : '');
            }
        }
    }

});