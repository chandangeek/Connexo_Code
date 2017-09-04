/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.store.CommandCategoriesForDeviceGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceMessageCategory'
    ],
    model: 'Mdc.model.DeviceMessageCategory',
    autoLoad: false,

    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devicegroups/{deviceGroupId}/commands',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceGroupId) {
            this.url = this.urlTpl.replace('{deviceGroupId}', deviceGroupId);
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});