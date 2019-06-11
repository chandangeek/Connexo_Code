/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'device',
        'status',
        {name: 'startedOn', type: 'date', dateFormat: 'time'},
        {name: 'finishedOn', type: 'date', dateFormat: 'time'}
    ],
    retryUrlTpl:  '../../api/tou/toucampaigns/retryDevice',
    cancelUrlTpl:  '../../api/tou/toucampaigns/cancelDevice',
    cancelUrl: function () {
        return this.cancelUrlTpl;
    },
    retryUrl: function () {
        return this.retryUrlTpl;
    },
    replaceIds: function(tpl){
        return tpl.replace('{deviceId}', this.get('device').id);
    }
});