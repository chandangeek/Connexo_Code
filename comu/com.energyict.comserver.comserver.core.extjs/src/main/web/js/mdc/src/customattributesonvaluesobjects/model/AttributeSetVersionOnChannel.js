/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/customproperties/{customPropertySetId}/versions'
    }
});
