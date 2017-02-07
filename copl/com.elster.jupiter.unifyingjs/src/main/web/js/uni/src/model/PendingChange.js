/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.PendingChange', {
    extend: 'Ext.data.Model',

    fields: [
        'attributeName',
        'originalValue',
        'newValue'
    ],
    proxy: {
        type: 'memory'
    }
});