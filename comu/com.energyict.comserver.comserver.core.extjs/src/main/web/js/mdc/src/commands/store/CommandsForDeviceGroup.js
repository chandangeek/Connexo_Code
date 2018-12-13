/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.store.CommandsForDeviceGroup', {
    extend: 'Ext.data.Store',
    model: 'Mdc.commands.model.CommandForDeviceGroup',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devicegroups/{deviceGroupId}/commands',
        reader: {
            type: 'json'
        },
        setUrl: function(deviceGroupId) {
            this.url = this.urlTpl.replace('{deviceGroupId}', deviceGroupId);
        }
    }
});