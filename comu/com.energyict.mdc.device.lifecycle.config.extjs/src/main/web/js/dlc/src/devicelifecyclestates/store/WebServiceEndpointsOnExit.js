/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.store.WebServiceEndpointsOnExit', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.WebServiceEndpoint'
    ],
    model: 'Dlc.devicelifecyclestates.model.WebServiceEndpoint',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    modelId : -1,
    removeAll: function(){
        this.callParent(arguments);
        this.modelId = -1;
    }
});
