/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.model.Issue', {
    extend: 'Isu.model.Issue',

    proxy: {
        type: 'rest',
        url: '/api/iws/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
