/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.ParentVersion', {
    extend: 'Uni.model.Version',
    fields: [
        {
            name: 'parent',
            type: 'auto',
            defaultValue: null
        }
    ]
});