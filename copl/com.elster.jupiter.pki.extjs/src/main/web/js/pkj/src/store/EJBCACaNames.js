/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.EJBCACaNames', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.Option',
    fields: [
        {name: 'options', type: 'auto', useNull: false}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/certificates/ejbca/caname/{endEntityId}',
        reader: {
            type: 'json',
            root: 'options'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (endEntityId) {
            this.url = this.urlTpl.replace('{endEntityId}', endEntityId);
        }
    }

});
