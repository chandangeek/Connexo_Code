/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.store.TimelineEntries', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.Timeline',
    autoLoad: false,
    proxy: 'memory'
});