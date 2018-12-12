/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComTask'
    ],
    model: 'Mdc.model.ComTask',
    storeId: 'ComTasks',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/comtasks',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});