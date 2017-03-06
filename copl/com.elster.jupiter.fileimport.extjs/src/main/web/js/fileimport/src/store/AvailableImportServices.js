/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.store.AvailableImportServices', {
    extend: 'Ext.data.Store',
    model: 'Fim.model.ImportService',
    proxy: {
        type: 'rest',
        url: '/api/fir/fields/fileupload',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
