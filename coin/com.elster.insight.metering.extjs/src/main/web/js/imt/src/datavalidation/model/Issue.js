/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.model.Issue', {
    extend: 'Isu.model.Issue',
    fields: [
        {name: 'usagePointInfo', type: 'auto'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issues',
        reader: {
            type: 'json'
        }
    }
});