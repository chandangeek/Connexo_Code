/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.store.AvailableWebServiceEndpoints', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.WebServiceEndpoint'
    ],
    model: 'Dlc.devicelifecyclestates.model.WebServiceEndpoint',
    storeId: 'AvailableWebServiceEndpoints',
    remoteFilter: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/statechangeendpointconfigurations',
        reader: {
            type: 'json',
            root: 'stateChangeEndPointConfigurations'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});