/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.Devices', {
    extend: 'Uni.data.store.Filterable',
    model: 'Tou.model.Device',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '../../api/tou/touCampaigns/{touCampaignId}/devices',
        reader: {
            type: 'json',
            root: 'devicesInCampaign'
        },
        setUrl: function (touCampaignId) {
            this.url = this.urlTpl.replace('{touCampaignId}', touCampaignId);
        }
    }
});