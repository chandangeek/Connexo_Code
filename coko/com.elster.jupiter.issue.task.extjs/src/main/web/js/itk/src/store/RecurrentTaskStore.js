/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.RecurrentTaskStore', {
    extend: 'Ext.data.Store',

    fields: [

        {name: 'id', type: 'auto'},
        {name: 'name', type: 'auto'},
        {name: 'queue', type: 'auto'},
        {name: 'displayType', type: 'auto'}
    ]
});