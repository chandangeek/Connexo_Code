/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.Certificates', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.CertificatesOverview',
        'Pkj.view.AddCertificate',
        'Pkj.view.CertificateDetails'
        //'Pkj.view.AddEditTrustStore',
        //'Pkj.view.ImportTrustedCertificate',
        //'Uni.view.window.Confirmation'
    ],
    stores: [
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

    currentCertificateId: undefined,

    init: function() {
        this.control({
            'certificates-overview certificates-grid': {
                select: this.onCertificateSelected
            },
            '#pkj-certificates-grid-add-certificate': {
                click: this.navigateToAddCertificatePage
            },
            'button#pkj-no-certificates-add-certificate-btn': {
                click: this.navigateToAddCertificatePage
            },
            '#pkj-certificate-add-form-add-btn': {
                click: this.addCertificate
            },
            '#pkj-certificates-grid-import-certificate': {
                click: this.navigateToImportCertificatePage
            }
        });
    },

    showCertificates: function() {
        var store = Ext.getStore('Pkj.store.Certificates');
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('certificates-overview',
                {
                    store: store,
                    router:this.getController('Uni.controller.history.Router')
                }
            )
        );
    },

    onCertificateSelected: function(grid, record) {
        var me = this;
        me.getCertificatePreview().loadRecordInForm(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('alias')));

    },

    navigateToAddCertificatePage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates/add').forward();
    },

    navigateToImportCertificatePage: function(menuItem) {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates/view/import').forward({certificateId: menuItem.up('menu').record.get('id')});
    },

    showAddCertificatePage: function() {
        this.getApplication().fireEvent('changecontentevent',
            Ext.widget('certificate-add',
                {
                    cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl()
                }
            )
        );
    },

    showImportCertificatePage: function(certificateId) {
        var me = this,
            certificateModel = Ext.ModelManager.getModel('Pkj.model.Certificate');

        certificateModel.load(certificateId, {
            success: function (certificateRecord) {
                me.currentCertificateId = certificateId;
                me.getApplication().fireEvent('changecontentevent',
                    Ext.widget('certificate-add',
                        {
                            cancelLink: me.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl(),
                            importMode: true
                        }
                    )
                );
                me.getApplication().fireEvent('certificateLoaded', certificateRecord.get('alias'));
            }
        });
    },

    addCertificate: function() {
        var me = this,
            form = me.getAddCertificateForm(),
            fileField = form.down('#pkj-certificate-add-form-file'),
            errorMsgPanel = form.down('uni-form-error-message'),
            maxFileSize = 2 * 1024,
            input = form.down('filefield').button.fileInputEl.dom,
            file = input.files[0],
            backUrl = me.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl();

        errorMsgPanel.hide();
        form.getForm().clearInvalid();

        if (!form.isValid()) {
            errorMsgPanel.show();
            if ( file!=undefined && file.size > maxFileSize) {
                fileField.markInvalid(Uni.I18n.translate('general.certificateFileTooBig', 'PKJ', 'File size should be less than 2 kB'));
            }
            return;
        }
        if (file.size > maxFileSize) {
            errorMsgPanel.show();
            fileField.markInvalid(Uni.I18n.translate('general.certificateFileTooBig', 'PKJ', 'File size should be less than 2 kB'));
            return;
        }

        form.setLoading();
        Ext.Ajax.request({
            url: '/api/pir/certificates/',
            method: 'POST',
            form: form.getEl().dom,
            params: {
                fileName: me.getFileName(form.down('filefield').getValue())
            },
            headers: {'Content-type': 'multipart/form-data'},
            isFormUpload: true,
            callback: function (config, success, response) {
                fileField.reset();
                if (response.responseText) {
                    var responseObject = JSON.parse(response.responseText);
                    if (!responseObject.success) {
                        me.getApplication().getController('Uni.controller.Error')
                            .showError(Uni.I18n.translate('general.certificateUploadFailed', 'PKJ', 'Failed to upload file'), responseObject.message);
                    }
                }
                form.setLoading(false);
                me.showCertificates();
            }
        });
    },

    getFileName: function (fullPath) {
        var filename = fullPath.replace(/^.*[\\\/]/, '');
        return filename;
    },

    showCertificateDetailsPage: function(certificateId) {
        var me = this,
            certificateModel = Ext.ModelManager.getModel('Pkj.model.Certificate'),
            widget = Ext.widget('certificate-details');

        certificateModel.load(certificateId, {
            success: function (certificateRecord) {
                me.currentCertificateId = certificateId;
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('certificate-preview').loadRecordInForm(certificateRecord);
                widget.down('certificate-preview').setTitle(certificateRecord.get('alias'));
                me.getApplication().fireEvent('certificateLoaded', certificateRecord.get('alias'));
            }
        });
    }
});