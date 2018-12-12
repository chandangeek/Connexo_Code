/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.DataPurgeSetting', {
    extend: 'Uni.model.Version',
    idProperty: 'kind',
    fields: [
        'kind',
        'name',
        'retainedPartitionCount',
        'retention'
    ]
});