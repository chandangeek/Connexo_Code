/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.model.WebServiceEndpoint', {
    extend: 'Ext.data.Model',
    alias: 'webServiceEndpoint',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'}
    ]
});