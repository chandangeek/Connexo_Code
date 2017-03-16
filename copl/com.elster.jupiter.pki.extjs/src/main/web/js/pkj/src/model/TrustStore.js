/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.TrustStore', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'name',
        'description'
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores',
        reader: {
            type: 'json'
        }
    }
});