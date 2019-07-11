/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.ManualIssue', {
    extend: 'Isu.model.Issue',

    proxy: {
        type: 'rest',
        url: '/api/isu/issues',
        reader: {
            type: 'json'
        }
    }
});