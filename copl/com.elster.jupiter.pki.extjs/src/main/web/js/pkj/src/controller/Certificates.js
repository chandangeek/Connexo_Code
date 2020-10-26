/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.controller.Certificates', {
    extend: 'Ext.app.Controller',

    requires: [
        'Pkj.view.RequestConfirmationWindow'
    ],

    views: [
        'Pkj.view.CertificatesOverview',
        'Pkj.view.AddCertificate',
        'Pkj.view.AddCSR',
        'Pkj.view.CertificateDetails'
    ],
    stores: [
        'Pkj.store.Certificates',
        'Pkj.store.KeyEncryptionMethods',
        'Pkj.store.CertificateTypes',
        'Pkj.store.EJBCAEndEntities',
        'Pkj.store.EJBCACaNames',
        'Pkj.store.EJBCACertProfiles'

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
            },
            '#pkj-obsolete-certificate-menu-item': {
                click: this.obsoleteCertificate
            },
            '#pkj-cancel-obsolete-certificate-menu-item': {
                click: this.cancelObsoleteCertificate
            },
            '#pkj-revoke-certificate-menu-item': {
                click: this.revokeCertificate
            },
            '#pkj-request-certificate-menu-item': {
                click: this.requestCertificate
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
            input = form.down('filefield').button.fileInputEl.dom,
            file = input.files[0];

        errorMsgPanel.hide();
        form.getForm().clearInvalid();

        if (!form.isValid()) {
            errorMsgPanel.show();
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
                if (!Ext.isEmpty(response.responseText)) {
                    var responseObject = JSON.parse(response.responseText);
                    if (!responseObject.success) {
                        if(responseObject.errors.length !== 0){
                            form.getForm().markInvalid(responseObject.errors);
                        } else {
                            me.getApplication().getController('Uni.controller.Error')
                                .showError(Uni.I18n.translate('general.certificateUploadFailed', 'PKJ', 'Failed to upload file'), responseObject.message, responseObject.errorCode);
                        }
                    }
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificateImport.success', 'PKJ', 'Certificate imported'));
                    me.getController('Uni.controller.history.Router').getRoute('administration/certificates').forward();
                }
                form.setLoading(false);

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
                widget.down('#pkj-certificate-details-panel').setTitle(Ext.String.htmlEncode(certificateRecord.get('alias')));
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
    },

    obsoleteCertificate: function (menuItem) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {confirmText: Uni.I18n.translate('general.confirm.obsolete', 'PKJ', 'Mark as obsolete')}),
            certificateRecord = menuItem.up('certificate-action-menu').record;

        Ext.Ajax.request({
            url: '/api/pir/certificates/' + certificateRecord.get('id') + '/markObsolete',
            method: 'POST',
            callback: function (config, success, response) {
                if (!Ext.isEmpty(response.responseText)) {
                    var responseObject = JSON.parse(response.responseText);

                    var messageConstructed = me.constructUsagesList(responseObject,
                        Uni.I18n.translate('certificate.usages.confirm.title', 'PKJ', 'Certificate is still used by the following objects') + ':',
                        Uni.I18n.translate('certificate.obsolete.confirm.question', 'PKJ', 'Do you want to mark it as obsolete?'));

                    confirmationWindow.insert(1,
                        {
                            xtype: 'displayfield',
                            itemId: 'obsolete-confirmation-field',
                            value: messageConstructed,
                            htmlEncode: false,
                            margin: '-15 0 10 50'
                        }
                    );

                    confirmationWindow.show({
                        title: Uni.I18n.translate('general.obsoleteX', 'PKJ', "Mark as obsolete '{0}'?", certificateRecord.get('alias')),
                        headers: {'Content-type': 'multipart/form-data'},
                        fn: function (state) {
                            if (state === 'confirm') {
                                Ext.Ajax.request({
                                    url: '/api/pir/certificates/' + certificateRecord.get('id') + '/forceMarkObsolete',
                                    method: 'POST',
                                    success: function () {
                                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificateMarkedObsolete', 'PKJ', 'Certificate marked as obsolete'));
                                        me.navigateToCertificatesOverview();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificateMarkedObsolete', 'PKJ', 'Certificate marked as obsolete'));
                    me.navigateToCertificatesOverview();
                }

            }
        });
    },

    cancelObsoleteCertificate: function (menuItem) {
        var me = this,
            certificateRecord = menuItem.up('certificate-action-menu').record;

        Ext.Ajax.request({
            url: '/api/pir/certificates/' + certificateRecord.get('id') + '/unmarkObsolete',
            method: 'POST',
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificateUnmarkedObsolete', 'PKJ', 'Certificate is no longer obsolete'));
                me.navigateToCertificatesOverview();
            }
        });
    },

    revokeCertificate: function (menuItem) {
        var me = this,
            certificateRecord = menuItem.up('certificate-action-menu').record,
            confirmationWindow = Ext.create('Pkj.view.RevocationConfirmationWindow', {
                    bindRecordId: certificateRecord.get('id'),
                    certificatesView: me
            });

        Ext.Ajax.request({
            url: '/api/pir/certificates/' + certificateRecord.get('id') + '/checkRevoke',
            method: 'POST',

            callback: function (config, success, response) {
                if (!Ext.isEmpty(response.responseText)) {
                    var responseObject = JSON.parse(response.responseText);
                    if (!Ext.isEmpty(responseObject.isUsed) && responseObject.isUsed === true) {
                        var errorMsg = me.constructUsagesList(responseObject,
                                Uni.I18n.translate('certificate.usages.error', 'PKJ', 'Certificate could not be revoked because it is still used on the following objects')+ ':');
                        me.getApplication().getController('Uni.controller.Error').showHtmlSensitiveError(
                            Uni.I18n.translate('general.actionUnavailableTitle', 'PKJ', "Couldn't perform your action"), errorMsg);
                        return;
                    }
                    confirmationWindow.show({
                        caOnline: !Ext.isEmpty(responseObject.isOnline) && responseObject.isOnline === true,
                        headers: {'Content-type': 'multipart/form-data'}
                    });
                }
            }
        });
    },

    requestCertificate: function (menuItem) {
        var me = this,
            recordId = menuItem.up('certificate-action-menu').record.get('id');

        Ext.widget('certificate-request-window', {
            confirmText: Uni.I18n.translate('general.request', 'PKJ', 'Request'),
            itemId: 'request-window',
            confirmation: function () {
                var self = this,
                    combobox = this.down('combobox'),
                    timeout = combobox.getValue();

                self.down('#request-progress').add(Ext.create('Ext.ProgressBar'))
                    .wait({
                        duration: timeout,
                        interval: 100,
                        increment: timeout / 100,
                        text: Uni.I18n.translate('certificate.revoke.progress.test', 'PKJ', 'Request to CA is in progress. Please wait...')
                    });
                self.down('#confirm-button').disable();
                self.down('#cancel-button').disable();
                combobox.disable();

                Ext.Ajax.request({
                    url: '/api/pir/certificates/' + recordId + '/requestCertificate?timeout=' + timeout,
                    method: 'POST',
                    timeout: timeout,

                    callback: function (config, success, response) {
                        if (success) {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('certificate.requestSuccess', 'PKJ', "Certificate requested"));
                            me.navigateToCertificatesOverview();
                        } else if (!Ext.isEmpty(response.responseText)) {
                            var responseObject = JSON.parse(response.responseText);
                            if (responseObject.isUsed) {
                                me.getApplication().getController('Uni.controller.Error').showHtmlSensitiveError(
                                    Uni.I18n.translate('general.actionUnavailableTitle', 'PKJ', "Couldn't perform your action"), Uni.I18n.translate('certificateRequest.usagesError', 'PKJ', 'A time out occurred. Certificate couldn\'t be received from the Certification authority.'));
                            }
                        }
                        self.close();
                    }
                });
            },
            green: true
        }).show({
            title: Uni.I18n.translate('general.certificateRequestQuestion', 'PKJ', 'Request certificate?'),
            msg: Uni.I18n.translate('general.requestCertificateWindowMsg', 'PKJ', 'The CSR will be sent to Certification authority'),
        });

    },

    constructUsagesList: function (certificateUsages, prefix, suffix) {
        var messageConstructed = '';
        var rowTemplate = '<li><b>{0}:</b> {1}</li>';
        if (prefix) {
            messageConstructed += prefix;
        }
        messageConstructed += '<br/><ul>';
        if (certificateUsages.securityAccessors.length !== 0) {
            messageConstructed += Ext.String.format(rowTemplate,
                Uni.I18n.translate('certificate.usages.confirm.accessors', 'PKJ', 'Security accessors'),
                certificateUsages.securityAccessors.join(', ') + (certificateUsages.securityAccessorsLimited === true ? '...' : ''));
        }
        if (certificateUsages.devices.length !== 0) {
            messageConstructed += Ext.String.format(rowTemplate,
                Uni.I18n.translate('certificate.usages.confirm.devices', 'PKJ', 'Devices'),
                certificateUsages.devices.join(', ') + (certificateUsages.devicesLimited === true ? '...' : ''));
        }
        if (certificateUsages.userDirectories.length !== 0) {
            messageConstructed += Ext.String.format(rowTemplate,
                Uni.I18n.translate('certificate.usages.confirm.directories', 'PKJ', 'User directories'),
                certificateUsages.userDirectories.join(', ') + (certificateUsages.userDirectoriesLimited === true ? '...' : ''));
        }
        messageConstructed += '</ul>';
        if (suffix) {
            messageConstructed += suffix;
        }
        return messageConstructed;
    }
});