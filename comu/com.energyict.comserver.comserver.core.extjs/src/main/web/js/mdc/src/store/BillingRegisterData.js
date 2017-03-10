/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.BillingRegisterData', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Mdc.model.BillingRegisterData'
    ],
    model: 'Mdc.model.BillingRegisterData',
    storeId: 'BillingRegisterData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/registers/{registerId}/data',
        reader: {
            type: 'json',
            root: 'data'
        }
    },
    setFilterModel: function (model) {
        var data = model.getData(),
            storeProxy = this.getProxy();
        storeProxy.setExtraParam('onlySuspect', data.onlySuspect);
        storeProxy.setExtraParam('onlyNonSuspect', data.onlyNonSuspect);
    }
});