/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.Devices', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fwc.firmwarecampaigns.model.Device',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/campaigns/{firmwareCampaignId}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        },
        setUrl: function (firmwareCampaignId) {
            this.url = this.urlTpl.replace('{firmwareCampaignId}', firmwareCampaignId);
        }
    }
});