/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.model.Issue', {
    extend: 'Isu.model.Issue',

    proxy: {
        type: 'rest',
        url: '/api/idv/issues',
        reader: {
            type: 'json'
        }
    }
});