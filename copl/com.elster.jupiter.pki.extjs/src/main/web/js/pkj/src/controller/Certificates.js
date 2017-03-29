/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.Certificates', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.CertificatesOverview',
        'Pkj.view.AddCertificate',
        'Pkj.view.AddCSR',
        'Pkj.view.CertificateDetails'
    ],
    stores: [
        'Pkj.store.Certificates',
        'Pkj.store.KeyEncryptionMethods',
        'Pkj.store.CertificateTypes'
    ],
    models: [
        'Pkj.model.Certificate',
        'Pkj.model.KeyEncryptionMethod',
        'Pkj.model.CertificateType',
        'Pkj.model.Csr'
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
        },
        {
            ref: 'addCSRForm',
            selector: 'csr-add csr-add-form'
        }
    ],

    currentCertificateId: undefined,
    onCertificateDetailsPage: false,

    init: function() {
        this.control({
            'certificates-overview certificates-grid': {
                select: this.onCertificateSelected
            },
            '#pkj-certificates-grid-add-certificate': {
                click: this.navigateToAddCertificatePage
            },
            '#pkj-certificates-grid-add-csr': {
                click: this.navigateToAddCSRPage
            },
            'button#pkj-no-certificates-add-certificate-btn': {
                click: this.navigateToAddCertificatePage
            },
            '#pkj-certificate-add-form-add-btn': {
                click: this.addCertificate
            },
            '#pkj-csr-add-form-add-btn': {
                click: this.addCSR
            },
            '#pkj-certificates-grid-import-certificate': {
                click: this.navigateToImportCertificatePage
            },
            '#pkj-import-certificate-menu-item': {
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
        this.onCertificateDetailsPage = false;
    },

    onCertificateSelected: function(grid, record) {
        var me = this;
        me.getCertificatePreview().loadRecordInForm(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('alias')));

    },

    navigateToCertificatesOverview: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates').forward();
    },

    navigateToAddCertificatePage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates/add').forward();
    },

    navigateToAddCSRPage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/certificates/addcsr').forward();
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

    showAddCSRPage: function() {
        var widget =  Ext.widget('csr-add',
                {
                    cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl()
                }
            ),
            encryptionMethodCombo = widget.down('#pkj-csr-add-form-key-encryption-method-combo'),
            typeCombo = widget.down('#pkj-csr-add-form-certificate-type-combo');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.getAddCSRForm().loadRecord(Ext.create('Pkj.model.Csr'));
        encryptionMethodCombo.getStore().load(function () {
            if (encryptionMethodCombo.getStore().getCount() > 0) {
                encryptionMethodCombo.select(encryptionMethodCombo.getStore().getAt(1));
            }
        });
        typeCombo.getStore().load(function () {
            if (typeCombo.getStore().getCount() > 0) {
                typeCombo.select(typeCombo.getStore().getAt(1));
            }
        });
    },

    showImportCertificatePage: function(certificateId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            certificateModel = Ext.ModelManager.getModel('Pkj.model.Certificate'),
            widget;

        certificateModel.load(certificateId, {
            success: function (certificateRecord) {
                me.currentCertificateId = certificateId;
                widget = Ext.widget('certificate-add',
                    {
                        cancelLink: me.onCertificateDetailsPage
                            ? router.getRoute('administration/certificates/view').buildUrl({certificateId: certificateId})
                            : router.getRoute('administration/certificates').buildUrl(),
                        importMode: true,
                        certificateRecord: certificateRecord
                    }
                );
                widget.down('#pkj-certificate-add-form-version-field').setValue(certificateRecord.get('version'));
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#pkj-certificate-add-form-alias').setValue(certificateRecord.get('alias'));
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
            file = input.files[0];

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
            url: form.importMode ? '/api/pir/certificates/' + form.certificateRecord.get('id') : '/api/pir/certificates/',
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

    addCSR: function() {
        var me = this,
            form = me.getAddCSRForm(),
            baseForm = form.getForm(),
            errorMsgPanel = form.down('uni-form-error-message'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            record = form.getRecord();

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsgPanel.hide();
        Ext.resumeLayouts(true);
        if (!form.isValid()) {
            errorMsgPanel.show();
            return;
        }

        viewport.setLoading();
        form.updateRecord(record);
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    viewport.setLoading(false);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.csrAdded', 'PKJ', 'CSR added'));
                    me.navigateToCertificatesOverview();
                } else {
                    if (responseText && responseText.errors) {
                        errorMsgPanel.show();
                        form.getForm().markInvalid(responseText.errors);
                    }
                }
            }
        });
    },

    showCertificateDetailsPage: function(certificateId) {
        var me = this,
            certificateModel = Ext.ModelManager.getModel('Pkj.model.Certificate'),
            widget = Ext.widget('certificate-details');

        certificateModel.load(certificateId, {
            success: function (certificateRecord) {
                me.currentCertificateId = certificateId;
                me.getApplication().fireEvent('changecontentevent', widget);
                me.onCertificateDetailsPage = true;
                widget.down('certificate-preview').loadRecordInForm(certificateRecord);
                if (widget.down('certificate-action-menu')) {
                    widget.down('certificate-action-menu').record = certificateRecord;
                }
                widget.down('#pkj-certificate-details-panel').setTitle(certificateRecord.get('alias'));
                me.getApplication().fireEvent('certificateLoaded', certificateRecord.get('alias'));
            }
        });
    }
});