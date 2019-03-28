/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.TouCampaigns', {
    extend: 'Ext.data.Store',
    model: 'Tou.model.TouCampaign',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tou/toucampaigns',
        api: {
             read: '/api/tou/toucampaigns',
             load: '/api/tou/toucampaigns',
             create: '/api/tou/toucampaigns/create',
             update: '/api/tou/toucampaigns/edit'
        },
        reader: {
            type: 'json',
            root: 'touCampaigns'
        }
    }
});