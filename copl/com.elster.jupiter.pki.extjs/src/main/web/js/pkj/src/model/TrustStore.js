/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.TrustStore', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'name',
        'description',
        {name: 'keyStoreFileSize', type: 'number', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores',
        reader: {
            type: 'json'
        }
    },

    doValidate: function (callback) {
        var data = this.getProxy().getWriter().getRecordData(this);
        delete data.firmwareFile;

        Ext.Ajax.request({
            method: 'POST',
            url: this.proxy.url + this.getId() + '/validate',
            callback: callback,
            jsonData: data
        });
    }

});