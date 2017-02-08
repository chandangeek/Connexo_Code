/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.model.Timeline', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        'user',
        'actionText',
        'creationDate',
        'forProcess',
        'processId',
        'contentText',
        'dateTime',
        'status',
        'processLink'
    ]
});