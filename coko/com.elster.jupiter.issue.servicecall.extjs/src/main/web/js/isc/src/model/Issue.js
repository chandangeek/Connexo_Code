/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        'parentServiceCall',
        'serviceCallType',
        'issueType',
        'receivedTime',
        'modTime',
        'onState'
    ],

    proxy: {
        type: 'rest',
        url: '/api/isc/issues',
        reader: {
            type: 'json',
        }
    }
});
