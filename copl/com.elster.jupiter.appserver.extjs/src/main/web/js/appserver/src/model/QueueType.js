/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.QueueType', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'value'
    ],
    idProperty: 'name'
});
