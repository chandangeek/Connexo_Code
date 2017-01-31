/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'device-firmware-action-menu',
    itemId: 'device-firmware-action-menu',
    shadow: false,
    plain: true,
    border: false,
    requires: [
        'Fwc.devicefirmware.store.FirmwareActions'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    store: 'Fwc.devicefirmware.store.FirmwareActions',

    getStoreListeners: function () {
        return {
            refresh: this.refresh
        };
    },

    initComponent: function () {
        var me = this;
        me.bindStore(me.store || 'ext-empty-store', true);
        me.callParent(arguments);
    },

    /**
     * @private
     * Refreshes the content of the checkbox group
     */
    refresh: function () {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll();

        if (me.store.getCount()) {
            me.store.each(function (record) {
                me.add({
                    text: record.get('localizedValue'),
                    itemId: record.getId(),
                    action: record.getId(),
                    record: record
                });
            });
        } else {
            me.up() && me.up().hide();
        }

        Ext.resumeLayouts();
    }
});
