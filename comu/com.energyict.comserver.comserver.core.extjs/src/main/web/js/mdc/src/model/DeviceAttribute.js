/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceAttribute', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'auto'},
        {name: 'mrid', type: 'auto'},
        {name: 'deviceType', type: 'auto'},
        {name: 'serialNumber', type: 'auto'},
        {name: 'deviceConfiguration', type: 'auto'},
        {name: 'yearOfCertification', type: 'auto'},
        {name: 'lifeCycleState', type: 'auto'},
        {name: 'batch', type: 'auto'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'serviceCategory', type: 'auto'},
        {name: 'shipmentDate', type: 'auto'},
        {name: 'installationDate', type: 'auto'},
        {name: 'deactivationDate', type: 'auto'},
        {name: 'decommissioningDate', type: 'auto'},
        {name: 'device', defaultValue: null},
        {name: 'multiplier', type: 'auto'},
        {name: 'geoCoordinates', type: 'auto'},
        {name: 'location', type: 'auto'},
        {
            name: 'deviceConfigurationDisplay',
            persist: false,
            mapping: function (data) {
                var res = {
                    attributeId: data.deviceConfiguration.attributeId,
                    displayValue: data.deviceConfiguration.displayValue,
                    deviceTypeId: data.deviceType.attributeId,
                    available: data.deviceConfiguration.available,
                    editable: data.deviceConfiguration.editable
                };

                return res
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}',
        reader: {
            type: 'json'
        }
    }
});
