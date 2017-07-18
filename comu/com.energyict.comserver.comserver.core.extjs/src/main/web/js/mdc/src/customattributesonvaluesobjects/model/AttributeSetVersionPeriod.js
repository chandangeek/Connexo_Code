/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'start', dateFormat: 'time', type: 'date'},
        {name: 'end', dateFormat: 'time', type: 'date'}
    ],

    proxy: {
        type: 'rest',

        setDeviceUrl: function (deviceId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId)).replace('{customPropertySetId}', customPropertySetId);
        },

        setChannelUrl: function (deviceId, channelId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/channels/{channelId}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId);
        },

        setRegisterUrl: function (deviceId, registerId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/registers/{registerId}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});
