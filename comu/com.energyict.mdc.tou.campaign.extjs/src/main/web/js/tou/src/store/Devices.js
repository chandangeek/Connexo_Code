/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.Devices', {
    extend: 'Uni.data.store.Filterable',
    model: 'Tou.model.Device',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: 'api/tou/touCampaigns/{touCampaignName}/devices',
        reader: {
            type: 'json',
        },
        setUrl: function (touCampaignName) {
            this.url = this.urlTpl.replace('{touCampaignName}', touCampaignName);
        }
    }
});