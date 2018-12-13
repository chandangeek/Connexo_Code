/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.FirmwareCampaigns', {
    extend: 'Ext.data.Store',
    model: 'Fwc.firmwarecampaigns.model.FirmwareCampaign',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fwc/campaigns',
        reader: {
            type: 'json',
            root: 'firmwareCampaigns'
        }
    }
});