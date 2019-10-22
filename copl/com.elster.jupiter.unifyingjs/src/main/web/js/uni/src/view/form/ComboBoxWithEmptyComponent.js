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

    prepareLoading: function (store) {
        return store;
    },

    initComponent: function () {
        var me = this;

        me.initConfig(me.config);
        me.store = Ext.data.StoreManager.lookup(me.getStore());
        me.store = me.prepareLoading(me.store);

        if(me.store.isStore && me.store.count()>0){
            var task = new Ext.util.DelayedTask(function(){
                me.storeLoaded(true);
            });
            task.delay(100);
        }
        else {

            me.store.load({
                callback: function (record, operation, success) {
                    me.storeLoaded(success);
                }
            });
        }

        me.callParent(arguments)
    },

    storeLoaded: function (success) {
        var me = this,
            combo;
        me.removeAll();
        if (success && me.store.count() > 0) {
            combo = Ext.create('Ext.form.ComboBox', {
                queryMode: 'local',
                name: me.config.name,
                store: me.store,
                allowBlank: me.config.allowBlank,
                width: me.config.width - me.getLabelWidth(),
                displayField: me.config.displayField,
                valueField: me.config.valueField,
                emptyText: me.config.emptyText,
                required: me.config.required,
                forceSelection: me.config.forceSelection,
                editable: me.config.editable,
                msgTarget: me.config.msgTarget,
                listeners: me.config.listeners
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
                    color: '#EB5642',
                    'margin-top': '6px'
                },
                validate: function () {
                    return me.config.allowBlank;
                },
                style: {
                    'margin-top': '0px !important'
                }
            });
            //me.style = {
            //    margin: '0 0 100px 0'
            //};
        }

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