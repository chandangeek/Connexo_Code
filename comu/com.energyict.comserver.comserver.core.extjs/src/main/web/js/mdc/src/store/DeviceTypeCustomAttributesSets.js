/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceTypeCustomAttributesSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',

    /*proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/customproperties',
        reader: {
            type: 'json',
            root: 'customproperties'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }*/
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/custompropertysets',

        reader: {
            type: 'json',
            root: 'deviceTypeCustomPropertySets'
        },

        extraParams: {
            filter: '[{"property":"linked","value":true},{"property":"specific","value":true}]'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});