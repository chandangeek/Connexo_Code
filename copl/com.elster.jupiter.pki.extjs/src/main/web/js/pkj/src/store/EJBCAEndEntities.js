/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.EJBCAEndEntities', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.Option',
    fields: [
        {name: 'options', type: 'auto', useNull: false}
    ],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/ejbca/endentities',
        reader: {
            type: 'json',
            root: 'options'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
    }

});
