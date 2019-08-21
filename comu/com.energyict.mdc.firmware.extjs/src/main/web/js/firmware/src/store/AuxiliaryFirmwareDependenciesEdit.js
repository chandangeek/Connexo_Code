/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.AuxiliaryFirmwareDependenciesEdit', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fwc.model.FirmwareAuxiliaryDependencyEdit',
    autoLoad: false,
    remoteFilter: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/field/firmwares/{campaignId}/previous',
        reader: {
            type: 'json',
            root: 'firmwares'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{campaignId}', deviceTypeId);
        }
    }
});