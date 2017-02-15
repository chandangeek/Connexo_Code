/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.TimelineEntries', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Timeline',
    autoLoad: false,
    proxy: 'memory'
});