/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.ConfigurationOptions', {
    extend: 'Ext.data.Store',
    model: 'Fwc.firmwarecampaigns.model.FirmvareVersionsOption',
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwaremanagementoptions',
        reader: {
            type: 'json',
            root: 'checkOptions'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    },
});