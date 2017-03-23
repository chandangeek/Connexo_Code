/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.Certificates', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.CertificatesOverview',
        'Pkj.view.AddCertificate',
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
        },
        {
            ref: 'addCertificateForm',
            selector: 'certificate-add certificate-add-form'
        }
    ],

    init: function() {
        this.control({
            'certificates-overview certificates-grid': {
                select: this.onCertificateSelected
            },
            'button#pkj-certificates-grid-add-certificate-btn': {
                click: this.navigateToAddCertificatePage
            },
            'button#pkj-certificate-add-form-add-btn': {
                click: this.addCertificate
            },
            'button#pkj-no-certificates-add-btn': {
                click: this.addCertificate
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
    },

    navigateToAddCertificatePage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates/add').forward();
    },

    showAddCertificatePage: function() {
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('certificate-add', {cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl()})
        );
    },

    addCertificate: function() {
        var me = this,
            form = me.getAddCertificateForm(),
            errorMsgPanel = form.down('uni-form-error-message');

        errorMsgPanel.hide();
        form.getForm().clearInvalid();
        if (!form.isValid()) {
            errorMsgPanel.show();
            return;
        }
    }

});