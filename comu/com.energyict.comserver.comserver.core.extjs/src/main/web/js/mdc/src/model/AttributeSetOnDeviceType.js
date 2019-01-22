/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.AttributeSetOnDeviceType', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    fields: [
        {name: 'deviceTypeName', type: 'string'},
        {name: 'deviceTypeVersion', type: 'integer'},
    ],

    proxy: {
        type: 'rest',
        //url: '/api/ddr/devices/{deviceId}/customproperties'
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/custompropertysets',
        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});