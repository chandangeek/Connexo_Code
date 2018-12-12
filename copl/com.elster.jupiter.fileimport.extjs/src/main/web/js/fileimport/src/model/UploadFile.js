/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.model.UploadFile', {
    extend: 'Ext.data.Model',
    fields: [
        'file',
        'scheduleId'
    ],
    proxy: {
        type: 'rest',
        url: '/api/fir/importservices/fileupload',
        reader: {
            type: 'json'
        }
    },
    doSave: function (callback, form) {
        Ext.Ajax.request({
            method: 'POST',
            headers: {'Content-type': 'multipart/form-data'},
            url: this.proxy.url,
            form: form.getEl(),
            isUpload: true,
            callback: callback
        });
    }
});
