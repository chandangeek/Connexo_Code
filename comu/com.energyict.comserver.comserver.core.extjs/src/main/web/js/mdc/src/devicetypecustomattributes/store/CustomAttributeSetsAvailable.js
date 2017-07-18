/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.devicetypecustomattributes.store.CustomAttributeSetsAvailable', {
    extend: 'Ext.data.Store',
    model: 'Mdc.devicetypecustomattributes.model.CustomAttributeSet',
    requires: [
        'Mdc.devicetypecustomattributes.model.CustomAttributeSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/custompropertysets',

        reader: {
            type: 'json',
            root: 'deviceTypeCustomPropertySets'
        },

        extraParams: {
            filter: '[{"property":"linked","value":false}]'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});