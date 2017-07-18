/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.CertificateAliases', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.CertificateAlias'
    ],
    model: 'Mdc.securityaccessors.model.CertificateAlias',
    pageSize: 50,
    autoLoad: false,
    proxy: {
        type: 'ajax',
        url: '',
        reader: {
            type: 'json',
            root: 'aliases'
        },
        setUrl: function (url) {
            this.url = url;
        }
    }
});