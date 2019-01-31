/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.AllowedDeviceTypeOptions', {
    extend: 'Ext.data.Store',
    requires: [
        'Tou.model.AllowedDeviceTypeOption'
    ],
    autoLoad: false,
    model: 'Tou.model.AllowedDeviceTypeOption',
    storeId: 'AllowedDeviceTypeOptionsStore',
    proxy: {
        type: 'rest',
        urlTpl: '../../api/tou/touCampaigns/getoptions?type={deviceTypeId}',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});