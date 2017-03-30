/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ObisCodeCombo', {
    extend: 'Uni.property.view.property.BaseCombo',

    referencesStore: Ext.create('Ext.data.Store', {
        fields: ['key', 'value', 'a', 'b', 'c', 'd', 'e', 'f', 'lastBillingPeriod', 'currentBillingPeriod', 'relativeBillingPeriod', 'ln']
    }),

    getEditCmp: function () {
        return this.isCombo() ? this.getComboCmp() : this.getNormalCmp();
    },

    /**
     * Return is component a combobox or not
     *
     * @returns {boolean}
     */
    isCombo: function () {
        return this.getProperty().getSelectionMode() === 'COMBOBOX';
    },

    getComboCmp: function () {
        var me = this;

        // clear store
        me.referencesStore.loadData([], false);
        _.map(me.getProperty().getPossibleValues(), function (item) {
            me.referencesStore.add({key: item.value, value: item.value, description: item.description, a: item.a, b: item.b, c: item.c, d: item.d, e: item.e, f: item.f,
                lastBillingPeriod: item.lastBillingPeriod, currentBillingPeriod: item.currentBillingPeriod, relativeBillingPeriod: item.relativeBillingPeriod, ln: item.ln});
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
                var result;

                if (value) {
                    result = Ext.Array.findBy(me.getProperty().getPossibleValues(), function (item) {
                        return value == item.value;
                    });
                    result = Ext.isObject(result) ? result.value : value;
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
            this.getField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.value : value : null);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.value : value : '');
            }
        }
    }

});