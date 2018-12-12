/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConflictingMappings', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConflictingMapping'
    ],
    model: 'Mdc.model.DeviceConflictingMapping',
    storeId: 'DeviceConflictingMappings',
    proxy: {
        type: 'rest',
        extraParams: {'all':'true'},
        urlTpl: '/api/dtc/devicetypes/{deviceType}/conflictmappings',
        reader: {
            type: 'json',
            root: 'conflictMapping'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceType}', encodeURIComponent(deviceTypeId));
        }
    }
});