/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.MessagesGridStore', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Message'
    ],
    model: 'Mdc.model.Message',
    storeId: 'MessagesGridStore'
});
