/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'conflicts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setChannelUrl: function (deviceId, channelId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/channels/{channelId}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{channelId}', channelId)
                .replace('{customPropertySetId}', customPropertySetId);
        },

        setChannelEditUrl: function (deviceId, channelId, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/channels/{channelId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{channelId}', channelId)
                .replace('{customPropertySetId}', customPropertySetId)
                .replace('{versionId}', versionId);
        },

        setRegisterUrl: function (deviceId, registerId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/registers/{registerId}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{registerId}', registerId)
                .replace('{customPropertySetId}', customPropertySetId);
        },

        setRegisterEditUrl: function (deviceId, registerId, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/registers/{registerId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{registerId}', registerId)
                .replace('{customPropertySetId}', customPropertySetId)
                .replace('{versionId}', versionId);
        },

        setDeviceUrl: function (deviceId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{customPropertySetId}', customPropertySetId);
        },

        setDeviceEditUrl: function (deviceId, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{deviceId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{deviceId}', encodeURIComponent(deviceId))
                .replace('{customPropertySetId}', customPropertySetId)
                .replace('{versionId}', versionId);
        }
    }
});