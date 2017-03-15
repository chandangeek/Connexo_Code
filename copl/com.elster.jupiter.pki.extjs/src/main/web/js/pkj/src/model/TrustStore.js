/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.TrustStore', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'description'
    ],
    idProperty: 'name',
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores',
        reader: {
            type: 'json'
        }
    }
});