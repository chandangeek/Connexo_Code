/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceConfiguration', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'description', type: 'string', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'loadProfileCount', type: 'number', useNull: true},
        {name: 'registerCount', type: 'number', useNull: true},
        {name: 'logBookCount', type: 'number', useNull: true},
        {name: 'canBeGateway', type: 'boolean', useNull: true},
        {name: 'isDirectlyAddressable', type: 'boolean', useNull: true},
        {name: 'gatewayType', type: 'string', useNull: true},
        {name: 'dataloggerEnabled', type: 'boolean', useNull: true},
        {name: 'validateOnStore', type: 'boolean', useNull: true}
    ],
    proxy: {
        timeout: 100000,
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations'
    }
});
