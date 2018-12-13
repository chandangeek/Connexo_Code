/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnRegister', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/registers/{registerId}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        }
    }
});