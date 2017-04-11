/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.InstallationDate', {
    extend: 'Uni.property.view.property.Date',


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

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'datefield',
            name: this.getName(),
            itemId: me.key + 'datefield',
            format: me.format,
            altFormats: me.formats.join('|'),
            width: me.width,
            maxWidth: 128,
            required: me.required,
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            blankText: me.blankText,
            editable: false,
            listeners: {
                change: function (fld, newValue) {
                    if (!newValue && !Ext.isDate(newValue)) {
                        fld.setValue(null);
                    } else {
                        me.fireEvent('reloadMeterRoleStore', newValue);
                    }
                },
                blur: function (fld) {
                    if (!Ext.isDate(fld.getValue())) {
                        me.restoreDefault();
                    }
                }
            }
        };
    },

    getField: function () {
        return this.down('datefield');
    },

    getValue: function () {
        if (Ext.isDate(this.getField().getValue())) {
            return this.getField().getValue().getTime()
        } else {
            return null;
        }
    },
});
