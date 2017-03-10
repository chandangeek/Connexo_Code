/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.EventType', {
    extend: 'Uni.model.Version',
    fields: [
        'topic',
        'component',
        'scope',
        'category',
        'name',
        'publish'
    ],
    idProperty: 'topic'
});