/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.FirmwareVersionsList', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Fwc.firmwarecampaigns.model.FirmwareVersionList'
    ],
    model: 'Fwc.firmwarecampaigns.model.FirmwareVersionList',
    storeId: 'Firmwares',
    autoLoad: false,
    remoteSort: true,
    hydrator: 'Uni.util.IdHydrator',
    pageSize: undefined,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/campaigns/{id}/firmwareversions',
        reader: {
            type: 'json',
            root: 'firmwareCampaignVersionStateInfos',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (firmwareCampaignId) {
            this.url = this.urlTpl.replace('{id}', firmwareCampaignId);
        }
    }
});
