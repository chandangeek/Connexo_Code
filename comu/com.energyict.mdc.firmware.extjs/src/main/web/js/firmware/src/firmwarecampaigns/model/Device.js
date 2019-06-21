/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        {name: 'device', type: 'auto'},
        'status',
        {name: 'startedOn', type: 'date', dateFormat: 'time'},
        {name: 'finishedOn', type: 'date', dateFormat: 'time'}
    ],
    cancelUrlTpl: '/api/fwc/devices/{deviceId}/firmwares/{campaignId}/cancel',
    retryUrlTpl:  '/api/fwc/devices/{deviceId}/firmwares/{campaignId}/retry',
    cancelUrl: function () {
        return this.replaceIds(this.cancelUrlTpl);
    },
    retryUrl: function () {
        return this.replaceIds(this.retryUrlTpl);
    },
    replaceIds: function(tpl){
        var url = tpl.replace('{campaignId}', this.get('id'));
        return url.replace('{deviceId}', this.get('device').name);
    }
});