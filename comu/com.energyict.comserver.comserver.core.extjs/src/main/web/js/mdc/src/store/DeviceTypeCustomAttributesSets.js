/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceTypeCustomAttributesSets', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.AttributeSetOnDeviceType',

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/custompropertysets',

        reader: {
            type: 'json',
            root: 'deviceTypeCustomPropertySets'
        },

        extraParams: {
            filter: '[{"property":"linked","value":true},{"property":"edit","value":true}]'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});