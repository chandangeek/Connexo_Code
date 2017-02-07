/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfilesOfDeviceData', {
    extend: 'Ext.data.Model',
    idProperty: 'interval_end',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'readingQualities', type: 'auto'},
        {name: 'channelData', type: 'auto'},
        {name: 'channelValidationData', type: 'auto'},
        {name: 'channelCollectedData', type: 'auto'},
        {name: 'validationActive', type: 'auto'},
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        }
    ],

    getDetailedInformation: function (device, channel, callback) {
        var me = this,
            record = this,
            channelsIds = _.keys(me.get('channelData')),
            requestCount = channelsIds.length,
            reading = me.get('interval').end,
            channelValidationData = me.get('channelValidationData'),
            doCallback = function (record) {
                requestCount--;
                if (!requestCount && Ext.isFunction(callback)) {
                    callback(record);
                }
            };

        if (requestCount) {
            Ext.Array.each(channelsIds, function (channelId) {
                Ext.Ajax.request({
                    url: '/api/ddr/devices/{device}/channels/{channel}/data/{reading}/validation'
                        .replace('{device}', device)
                        .replace('{channel}', channelId)
                        .replace('{reading}', reading),
                    success: function(response) {
                        var data = Ext.decode(response.responseText);
                        Ext.merge(channelValidationData[channelId], data);

                        record.beginEdit();
                        record.set('validationActive', record.get('validationActive') || channelValidationData[channelId].validationActive);
                        record.endEdit();
                        doCallback(record);
                    }
                })
            });
        } else if (Ext.isFunction(callback)) {
            callback();
        }
    }
});