/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.form.ComboBoxWithEmptyComponent
 * This is a combobox which will show a text if the store of the combobox is empty
 *
 * Example:
 *   {
 *      xtype: 'comboboxwithemptycomponent',
 *      fieldLabel: 'Select users',
 *      store: 'App.store.Users',
 *      columns: 1,
 *      name: 'users'
 *   }
 */
Ext.define('Uni.view.form.ComboBoxWithEmptyComponent', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.comboboxwithemptycomponent',
    myEvents: [],
    width: null,

    config: {
        noObjectsText: Uni.I18n.translate('general.noObjectsDefinedYet', 'UNI', 'No objects defined yet'),
        store: null,
        name: null,
        displayField: null,
        valueField: null,
        allowBlank: false,
        editable: false,
        forceSelection: true,
        required: false,
        width: 650,
        msgTarget: 'under',
        emptyText: Uni.I18n.translate('general.selectAnObject', 'UNI', 'Select an object...')
    },

    style: {
        margin: '1px 0 0 0'
    },

    initComponent: function () {
        var me = this,
            storeObject,
            combo;
        me.initConfig(me.config);
        storeObject = Ext.data.StoreManager.lookup(me.getStore());
        storeObject.load({
            callback: function (record, operation, success) {
                me.removeAll();
                if (success && storeObject.count() > 0) {
                    combo = Ext.create('Ext.form.ComboBox', {
                        queryMode: 'local',
                        name: me.config.name,
                        store: storeObject,
                        allowBlank: me.config.allowBlank,
                        width: me.config.width - me.getLabelWidth(),
                        displayField: me.config.displayField,
                        valueField: me.config.valueField,
                        emptyText: me.config.emptyText,
                        required: me.config.required,
                        forceSelection: me.config.forceSelection,
                        editable: me.config.editable,
                        msgTarget: me.config.msgTarget
                    });
                    Ext.Array.each(me.myEvents, function(event) {
                        combo.on(event.event, event.func, event.scope);
                    });
                    me.add(combo);
                } else {
                    me.add({
                        xtype: 'displayfield',
                        itemId: 'noObjectsFoundField',
                        fieldLabel: '',
                        value: me.getNoObjectsText(),
                        fieldStyle: {
                            color: '#EB5642'
                        },
                        validate: function () {
                            return me.config.allowBlank;
                        }
                    });
                    //me.style = {
                    //    margin: '0 0 100px 0'
                    //};
                }

            }
        });

        me.callParent(arguments)
    },

    onComboEvent: function(event, func, scope) {
        var me = this;
        if(me.down('combobox')) {
            me.down('combobox').on(event, func, scope);
        } else {
            me.myEvents.push({event: event, func: func, scope:scope});
        }
    },

    getValue: function() {
        var me = this;
        if(me.down('combobox')) {
            return me.down('combobox').getValue();
        } else {
            return undefined
        }
    }
});