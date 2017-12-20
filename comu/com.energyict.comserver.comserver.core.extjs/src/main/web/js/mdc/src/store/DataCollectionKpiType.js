/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Mdc.store.DataCollectionKpiType', {
    extend: 'Ext.data.ArrayStore',
    fields: ['id', 'name'],
    data: [
        ['connection', Uni.I18n.translate('datacollectionkpis.connectionTask', 'MDC', 'Connection task')],
        ['communication', Uni.I18n.translate('datacollectionkpis.communicationTaks', 'MDC', 'Communication task')]
    ],
    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});
