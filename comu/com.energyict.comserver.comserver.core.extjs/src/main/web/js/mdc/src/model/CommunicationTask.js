/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CommunicationTask',{
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string', useNull: true},
        {name:'inUse', type: 'boolean', useNull: true},
        {name:'commands', type: 'auto', useNull: true, defaultValue: null},
        {name:'messages', type: 'auto', useNull: true, defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks',
        reader: {
            type: 'json'
        }
    }
});
