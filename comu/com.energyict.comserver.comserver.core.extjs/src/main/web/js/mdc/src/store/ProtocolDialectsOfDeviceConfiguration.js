/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ProtocolDialectsOfDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ProtocolDialect'
    ],
    model: 'Mdc.model.ProtocolDialect',
    storeId: 'ProtocolDialectsOfDeviceConfiguration',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/protocoldialects',
        reader: {
            type: 'json',
            root: 'protocolDialects'
        }
    }
});
