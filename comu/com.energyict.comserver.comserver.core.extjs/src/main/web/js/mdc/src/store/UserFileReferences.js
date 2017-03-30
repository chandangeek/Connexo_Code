/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.UserFileReferences', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.UserFileReference'
    ],
    model: 'Mdc.model.UserFileReference',
    storeId: 'UserFileReferences',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/plr/userfilereferences',
        reader: {
            type: 'json',
            root: 'UserFile'
        }
    }
});