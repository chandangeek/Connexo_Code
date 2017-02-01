/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.PhaseCodes', {
    extend: 'Ext.data.Store',
    fields: ['id', 'displayValue'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/phasecodes',
        reader: {
            type: 'json',
            root: 'phaseCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});