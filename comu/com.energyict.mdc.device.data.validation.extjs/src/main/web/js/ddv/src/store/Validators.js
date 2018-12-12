/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/10/2016.
 */
Ext.define('Ddv.store.Validators', {
    extend: 'Ext.data.Store',
    fields: [
        'id', 'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddq/fields/validators',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'validators'
        }
    }
});
