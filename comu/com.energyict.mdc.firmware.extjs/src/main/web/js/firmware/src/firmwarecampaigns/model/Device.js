/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'campaignId',
        'device',
        'status',
        {name: 'startedOn', type: 'date', dateFormat: 'time'},
        {name: 'finishedOn', type: 'date', dateFormat: 'time'}
    ],
    cancelUrlTpl: '/api/fwc/devices/{deviceId}/firmwares/{campaignId}/cancel',
    retryUrlTpl: '/api/fwc/devices/{deviceId}/firmwares/{campaignId}/retry',
    cancelUrl: function (campaignId) {
        return this.replaceIds(this.cancelUrlTpl, campaignId);
    },
    retryUrl: function (campaignId) {
        return this.replaceIds(this.retryUrlTpl, campaignId);
    },
    replaceIds: function (tpl, campaignId) {
        var url = tpl.replace('{campaignId}', campaignId);
        return url.replace('{deviceId}', this.get('device').name);
    }
});
