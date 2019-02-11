/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.TouCampaigns', {
    extend: 'Ext.data.Store',
    model: 'Tou.model.TouCampaign',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tou/touCampaigns',
        api: {
             read: '/api/tou/touCampaigns',
             load: '/api/tou/touCampaigns',
             create: '/api/tou/touCampaigns/create',
             update: '/api/tou/touCampaigns/edit'
        },
        reader: {
            type: 'json',
            root: 'touCampaigns'
        }
    }
});