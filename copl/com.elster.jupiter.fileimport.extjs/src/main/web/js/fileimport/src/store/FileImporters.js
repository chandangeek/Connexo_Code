/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.store.FileImporters', {
    extend: 'Ext.data.Store',
    model: 'Fim.model.FileImporter',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fir/importers',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'fileImporters'
        }
    }
});
