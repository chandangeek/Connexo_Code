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
    retryUrlTpl:  '../../api/tou/touCampaigns/retryDevice',
    cancelUrlTpl:  '../../api/tou/touCampaigns/cancelDevice',
    cancelUrl: function () {
        return this.cancelUrlTpl;
    },
    retryUrl: function () {
        return this.retryUrlTpl;
    },
    replaceIds: function(tpl){
        //var url = tpl.replace('{campaignName}', this.get('campaignName'));
        return tpl.replace('{deviceId}', this.get('device').id);
    }
});