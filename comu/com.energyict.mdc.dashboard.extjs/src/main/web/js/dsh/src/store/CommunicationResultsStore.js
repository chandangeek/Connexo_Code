/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.CommunicationResultsStore', {
    extend: 'Uni.data.store.Filterable',
    model: 'Dsh.model.ConnectionResults',
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationheatmap',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
        }
    }
});