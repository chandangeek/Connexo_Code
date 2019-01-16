/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'device',
        'status',
        {name: 'startedOn', type: 'date', dateFormat: 'time'},
        {name: 'finishedOn', type: 'date', dateFormat: 'time'}
    ],
    //cancelUrlTpl: '/api/fwc/devices/{deviceId}/firmwares/{campaignId}/cancel',
    retryUrlTpl:  'api/tou/touCampaigns/retry/{deviceId}',
    cancelUrl: function () {
        return this.replaceIds(this.cancelUrlTpl);
    },
    retryUrl: function () {
        return this.replaceIds(this.retryUrlTpl);
    },
    replaceIds: function(tpl){
        //var url = tpl.replace('{campaignName}', this.get('campaignName'));
        return tpl.replace('{deviceId}', this.get('device').id);
    }
});