/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.InstallationDate', {
    extend: 'Uni.property.view.property.DateTime',


    listeners: {
        afterrender: {
            fn: function () {
                var store = Ext.getStore('Imt.processes.store.AvailableMeterRoles');
                store.events['loadmeterrolestore'] && store.events['loadmeterrolestore'].clearListeners();
                store.events['clearmeterrolestore'] && store.events['clearmeterrolestore'].clearListeners();

            }
        },
        reloadMeterRoleStore: {
            fn: function (date) {
                var me = this,
                    store = Ext.getStore('Imt.processes.store.AvailableMeterRoles');
                if(Ext.isDate(date)){
                    store.getProxy().setUrl(me.up('property-form').context.id, date.getTime());
                    store.fireEvent('loadmeterrolestore');
                } else {
                    store.fireEvent('clearmeterrolestore');
                }
            }
        }
    },

    restoreDefault: function () {
        this.fireEvent('reloadMeterRoleStore', null);
        this.callParent(arguments);
    },

    initListeners: function () {
        var me = this,
            dateField = me.getField();

        if (dateField) {
            dateField.on('change', function (fld, newValue) {
                if (!newValue && !Ext.isDate(newValue)) {
                    fld.setValue(null);
                } else {
                    me.fireEvent('reloadMeterRoleStore', newValue);
                }
            });
            dateField.on('blur', function (fld, newValue) {
                if (!Ext.isDate(fld.getValue())) {
                    me.restoreDefault();
                }
            });
        }
        this.callParent(arguments);
    },

    getField: function () {
        return this.down('datefield');
    }
});
