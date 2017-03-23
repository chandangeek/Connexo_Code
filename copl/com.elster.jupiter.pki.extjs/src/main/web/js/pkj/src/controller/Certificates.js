/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.Certificates', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.CertificatesOverview'
        //'Pkj.view.TrustedCertificatesView',
        //'Pkj.view.AddEditTrustStore',
        //'Pkj.view.ImportTrustedCertificate',
        //'Uni.view.window.Confirmation'
    ],
    stores: [
        //'Pkj.store.TrustStores',
        'Pkj.store.Certificates'
    ],
    models: [
        'Pkj.model.Certificate'
    ],

    refs: [
        {
            ref: 'certificatePreview',
            selector: 'certificates-overview certificate-preview'
        },
        {
            ref: 'certificatePreviewForm',
            selector: 'certificates-overview certificate-preview form'
        }
    ],

    init: function() {
        this.control({
            'certificates-overview certificates-grid': {
                select: this.onCertificateSelected
            }
        });
    },

    showCertificates: function() {
        var store = Ext.getStore('Pkj.store.Certificates');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('certificates-overview', {store: store}));
    },

    onCertificateSelected: function(grid, record) {
        var me = this;
        me.getCertificatePreviewForm().loadRecord(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('alias')));
    }

});