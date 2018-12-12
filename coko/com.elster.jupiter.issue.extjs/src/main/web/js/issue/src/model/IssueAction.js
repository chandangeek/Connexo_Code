/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.IssueAction', {
    extend: 'Isu.model.Action',
    fields: [
        {name: 'issue', defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        reader: {
            type: 'json'
        },
        timeout: 300000
    }
});
