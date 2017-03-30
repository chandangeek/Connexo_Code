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
            '#pkj-no-certificates-add-csr-btn': {
                click: this.navigateToAddCSRPage
            },
            '#pkj-csr-add-form-add-btn': {
                click: this.addCSR
            },
            '#pkj-certificates-grid-import-certificate': {
                click: this.navigateToImportCertificatePage
            },
            '#pkj-import-certificate-menu-item': {
                click: this.navigateToImportCertificatePage
            },
            '#pkj-download-csr-menu-item': {
                click: this.downloadCSR
            },
            '#pkj-download-certificate-menu-item': {
                click: this.downloadCertificate
            },
            '#pkj-remove-certificate-menu-item': {
                click: this.removeCertificate
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
        var me = this,
            menu = me.getCertificatePreview().down('certificate-action-menu');

        me.getCertificatePreview().loadRecordInForm(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('alias')));
        if (menu) {
            menu.record = record;
        }
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
        var me = this,
            widget =  Ext.widget('csr-add',
                {
                    cancelLink: this.getController('Uni.controller.history.Router').getRoute('administration/certificates').buildUrl()
                }
            ),
            csrRecord2Load = Ext.create('Pkj.model.Csr'),
            encryptionMethodCombo = widget.down('#pkj-csr-add-form-key-encryption-method-combo'),
            typeCombo = widget.down('#pkj-csr-add-form-certificate-type-combo'),
            keyEncryptionMethodsStore = Ext.getStore('Pkj.store.KeyEncryptionMethods'),
            typesStore = Ext.getStore('Pkj.store.CertificateTypes'),
            onEncryptionMethodsLoaded = function(store, records, successful) {
                if (successful && store.getCount()===1) {
                    csrRecord2Load.keyTypeId = store.getAt(0).get('id');
                }
                typesStore.on('load', onTypesLoaded, me, {single:true});
                typesStore.load();
            },
            onTypesLoaded = function(store, records, successful) {
                if (successful && store.getCount()===1) {
                    csrRecord2Load.keyEncryptionMethod = store.getAt(0).get('id');
                }
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getAddCSRForm().loadRecord(csrRecord2Load);
                if (!Ext.isEmpty(csrRecord2Load.keyTypeId)) {
                    encryptionMethodCombo.select(csrRecord2Load.keyTypeId);
                }
                if (!Ext.isEmpty(csrRecord2Load.keyEncryptionMethod)) {
                    typeCombo.select(csrRecord2Load.keyEncryptionMethod);
                }
            };

        keyEncryptionMethodsStore.on('load', onEncryptionMethodsLoaded, me, {single:true});
        keyEncryptionMethodsStore.load();
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
                viewport.setLoading(false);
                if (success) {
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

    downloadCSR: function(menuItem) {
        var certificateRecord = menuItem.up('certificate-action-menu').record,
            url = '/api/pir/certificates/' + certificateRecord.get('id') + '/download/csr';
        window.open(url, '_blank');
    },

    downloadCertificate: function(menuItem) {
        var certificateRecord = menuItem.up('certificate-action-menu').record,
            url = '/api/pir/certificates/' + certificateRecord.get('id') + '/download/certificate';
        window.open(url, '_blank');
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
    },

    removeCertificate: function(menuItem) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            certificateRecord = menuItem.up('certificate-action-menu').record;

        confirmationWindow.show({
            title: Uni.I18n.translate('general.removeX', 'PKJ', "Remove '{0}'?", certificateRecord.get('alias')),
            msg: Uni.I18n.translate('certificate.remove.msg', 'PKJ', 'The certificate will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    certificateRecord.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificateRemoved', 'PKJ', 'Certificate removed'));
                            me.navigateToCertificatesOverview();
                        }
                    });
                }
            }
        });
    }

});