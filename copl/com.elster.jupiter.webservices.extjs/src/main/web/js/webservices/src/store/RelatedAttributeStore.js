/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Wss.store.RelatedAttributeStore', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.RelatedAttributeModel',
    autoLoad: false,
    pageSize: 50
});