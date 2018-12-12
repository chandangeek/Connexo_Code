/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTaskConfigsOfDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTaskConfig'
    ],
    model: 'Mdc.model.CommunicationTaskConfig',
    storeId: 'CommunicationTaskConfigsOfDeviceConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/comtaskenablements',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});