/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.TrustStores', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.TrustStoresOverview',
        'Pkj.view.TrustedCertificatesView',
        'Pkj.view.AddEditTrustStore',
        'Pkj.view.ImportTrustedCertificate',
        'Uni.view.window.Confirmation'
    ],
    stores: [
        'Pkj.store.TrustStores',
        'Pkj.store.TrustedCertificates'
    ],
    models: [
        'Pkj.model.TrustStore'
    ],

    refs: [
        {
            ref: 'addPage',
            selector: 'truststore-add'
        },
        {
            ref: 'addForm',
            selector: 'truststore-add form'
        },
        {
            ref: 'trustStorePreview',
            selector: 'truststores-overview truststore-preview'
        },
        {
            ref: 'trustStorePreviewForm',
            selector: 'truststores-overview truststore-preview truststore-preview-form'
        },
        {
            ref: 'certificateImportForm',
            selector: 'trusted-certificate-import-form'
        },
        {
            ref: 'trustedCertificatePreview',
            selector: 'truststore-certificates-view trusted-certificate-preview'
        },
        {
            ref: 'trustedCertificatePreviewForm',
            selector: 'truststore-certificates-view trusted-certificate-preview trusted-certificate-preview-form'
        }
    ],

    currentTrustStoreId: undefined,
    onTrustStoreCertificatesPage: false,

    init: function() {
        this.control({
            'truststores-overview truststores-grid': {
                select: this.onTrustStoreSelected
            },
            'button#pkj-add-truststore-btn': {
                click: this.navigateToAddTrustStore
            },
            'button#pkj-add-truststore-add-btn': {
                click: this.addOrEditTrustStore
            },
            '#pkj-truststores-grid-add-truststore': {
                click: this.navigateToAddTrustStore
            },
            'truststore-action-menu': {
                click: this.onStoreMenuAction
            },
            'button#pkj-certificates-grid-import-certificates': {
                click: this.navigateToImportTrustedCertificates
            },
            'button#pkj-import-certificates-step': {
                click: this.navigateToImportTrustedCertificates
            },
            'button#pkj-trusted-certificate-import-form-import-btn': {
                click: this.importCertificates
            },
            'truststores-certificates-grid': {
                select: this.onTrustedCertificateSelected
            },
            'trusted-certificate-action-menu': {
                click: this.onCertificateMenuAction
            }
        });
    },

    showTrustStores: function() {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('truststores-overview', {router: this.getController('Uni.controller.history.Router')}));
        this.onTrustStoreCertificatesPage = false;
    },

    navigateToTrustStoresOverviewPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/truststores').forward();
    },

    navigateToAddTrustStore: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/truststores/add').forward();
    },

    navigateToEditTrustStore: function(record) {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/truststores/edit')
            .forward({trustStoreId: record.get('id')});
    },

    navigateToImportTrustedCertificates: function() {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/truststores/view/importcertificates')
            .forward({trustStoreId: this.currentTrustStoreId});
    },

    navigateToTrustStoreAndCertificatesPage: function() {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/truststores/view')
            .forward({trustStoreId: this.currentTrustStoreId});
    },

    showAddTrustStore: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('truststore-add', {
                action: 'add',
                returnLink: router.getRoute('administration/truststores').buildUrl()
            });
        view.down('form').loadRecord(Ext.create('Pkj.model.TrustStore'));
        me.getApplication().fireEvent('changecontentevent', view);
    },

    addOrEditTrustStore: function() {
        var me = this,
            form = me.getAddForm(),
            baseForm = form.getForm(),
            errorMessage = form.down('#pkj-add-truststore-error-form'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            record = form.getRecord();

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        if (!form.isValid()) {
            errorMessage.show();
            return;
        }

        viewport.setLoading();
        form.updateRecord(record);
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    viewport.setLoading(false);
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('truststores.saveSuccess', 'PKJ', 'Trust store saved')
                        : Uni.I18n.translate('truststores.addSuccess', 'PKJ', 'Trust store added'));
                    if (operation.action !== 'update') {
                        me.currentTrustStoreId = record.get('id');
                        me.navigateToTrustStoreAndCertificatesPage();
                    } else if (me.onTrustStoreCertificatesPage) {
                        me.navigateToTrustStoreAndCertificatesPage();
                    } else {
                        me.navigateToTrustStoresOverviewPage();
                        me.onTrustStoreCertificatesPage = false;
                    }
                } else {
                    if (responseText && responseText.errors) {
                        errorMessage.show();
                        form.getForm().markInvalid(responseText.errors);
                    }
                    viewport.setLoading(false);
                }
            }
        });
    },

    onTrustStoreSelected: function(grid, record) {
        var me = this;
        me.getTrustStorePreviewForm().loadRecord(record);
        me.getTrustStorePreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (me.getTrustStorePreview().down('truststore-action-menu')) {
            me.getTrustStorePreview().down('truststore-action-menu').record = record;
        }
    },

    onStoreMenuAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'editTrustStore':
                me.navigateToEditTrustStore(menu.record);
                break;
            case 'removeTrustStore':
                me.removeTrustStore(menu.record);
                break;
            case 'importTrustedCertificates':
                me.currentTrustStoreId = menu.record.get('id');
                this.navigateToImportTrustedCertificates();
                break;
        }
    },

    showEditTrustStore: function(trustStoreId) {
        var me = this,
            model = Ext.ModelManager.getModel('Pkj.model.TrustStore');
        model.load(trustStoreId, {
            success: function (record) {
                me.showEditTrustStorePage(record);
                me.getApplication().fireEvent('trustStoreLoaded', record.get('name'));
            }
        });
    },

    showEditTrustStorePage: function(trustStoreRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('truststore-add', {
                action: 'edit',
                returnLink: me.onTrustStoreCertificatesPage
                    ? router.getRoute('administration/truststores/view').buildUrl({trustStoreId:trustStoreRecord.get('id')})
                    : router.getRoute('administration/truststores').buildUrl()
            });

        view.down('panel').setTitle(Ext.String.format(Uni.I18n.translate('general.editX', 'PKJ', "Edit '{0}'"), trustStoreRecord.get('name')));
        view.down('form').loadRecord(trustStoreRecord);
        me.getApplication().fireEvent('changecontentevent', view);
    },

    removeTrustStore: function(trustStoreRecord) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            title: Uni.I18n.translate('general.removeX', 'PKJ', "Remove '{0}'?", trustStoreRecord.get('name')),
            msg: Uni.I18n.translate('truststores.remove.msg', 'PKJ', 'The trust store and its certificates will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    trustStoreRecord.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.trustStoreRemoved', 'PKJ', 'Trust store removed'));
                            me.navigateToTrustStoresOverviewPage();
                        }
                    });
                }
            }
        });
    },

    showTrustedStoreAndCertificates: function(trustStoreId) {
        var me = this,
            model = Ext.ModelManager.getModel('Pkj.model.TrustStore'),
            certificatesStore = Ext.getStore('Pkj.store.TrustedCertificates');

        certificatesStore.getProxy().setExtraParam('trustStoreId', trustStoreId);
        model.load(trustStoreId, {
            success: function (record) {
                me.currentTrustStoreId = trustStoreId;
                certificatesStore.load(function() {
                    me.showTrustedStoreAndCertificatesPage(record, certificatesStore);
                    me.getApplication().fireEvent('trustStoreLoaded', record.get('name'));
                });
            }
        });
    },

    showTrustedStoreAndCertificatesPage: function(trustStoreRecord, certificateStore) {
        var me = this,
            view = Ext.widget('truststore-certificates-view', {store:certificateStore});

        me.onTrustStoreCertificatesPage = true;
        view.loadTrustStoreRecord(trustStoreRecord);
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showImportCertificatesPage: function(trustStoreId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = Ext.ModelManager.getModel('Pkj.model.TrustStore');

        me.currentTrustStoreId = trustStoreId;
        model.load(trustStoreId, {
            success: function (trustStoreRecord) {
                me.getApplication().fireEvent('trustStoreLoaded', trustStoreRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent',
                    Ext.widget('trusted-certificate-import', {
                        cancelLink: me.onTrustStoreCertificatesPage
                            ? router.getRoute('administration/truststores/view').buildUrl({trustStoreId:me.trustStoreId})
                            : router.getRoute('administration/truststores').buildUrl(),
                        trustStoreRecord: trustStoreRecord
                    })
                );
                me.onTrustStoreCertificatesPage = false;
            }
        });
    },

    importCertificates: function() {
        var me = this,
            form = me.getCertificateImportForm(),
            fileField = form.down('#pkj-trusted-certificate-import-form-file'),
            errorMsgPanel = form.down('uni-form-error-message'),
            maxFileSize = 250 * 1024,
            input = form.down('filefield').button.fileInputEl.dom,
            file = input.files[0];

        errorMsgPanel.hide();
        form.getForm().clearInvalid();

        if (!form.isValid()) {
            errorMsgPanel.show();
            if ( file!=undefined && file.size > maxFileSize) {
                fileField.markInvalid(Uni.I18n.translate('general.keyStoreFileTooBig', 'PKJ', 'File size should be less than 250 kB'));
            }
            return;
        }
        if (file.size > maxFileSize) {
            errorMsgPanel.show();
            fileField.markInvalid(Uni.I18n.translate('general.keyStoreFileTooBig', 'PKJ', 'File size should be less than 250 kB'));
            return;
        }

        form.setLoading();
        Ext.Ajax.request({
            url: '/api/pir/truststores/' + me.currentTrustStoreId + '/certificates/keystore',
            method: 'POST',
            form: form.getEl().dom,
            params: {
                fileName: me.getFileName(form.down('filefield').getValue())
            },
            headers: {'Content-type': 'multipart/form-data'},
            isFormUpload: true,
            callback: function (config, success, response) {
                form.setLoading(false);
                if (response.responseText) {
                    var json = Ext.decode(response.responseText, true);
                    if (json && json.errors) {
                        Ext.suspendLayouts();
                        fileField.reset();
                        errorMsgPanel.show();
                        form.getForm().markInvalid(json.errors);
                        Ext.resumeLayouts(true);
                    }
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.certificatesImport.success', 'MDC', 'Certificates imported'));
                    me.navigateToTrustStoreAndCertificatesPage();
                }
            }
        });
    },

    getFileName: function (fullPath) {
        var filename = fullPath.replace(/^.*[\\\/]/, '');
        return filename;
    },

    onTrustedCertificateSelected: function(grid, record) {
        var me = this,
            actionMenu = me.getTrustedCertificatePreview().down('trusted-certificate-action-menu');

        me.getTrustedCertificatePreviewForm().loadRecord(record);
        me.getTrustedCertificatePreview().setTitle(Ext.htmlEncode(record.get('alias')));
        if (actionMenu) {
            actionMenu.record = record;
        }
    },

    onCertificateMenuAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'downloadTrustedCertificate':
                me.downloadCertificate(menu.record);
                break;
            case 'removeTrustedCertificate':
                me.removeCertificate(menu.record);
                break;
        }
    },

    removeCertificate: function(certificateRecord) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            title: Uni.I18n.translate('general.removeX', 'PKJ', "Remove '{0}'?", certificateRecord.get('alias')),
            msg: Uni.I18n.translate('trustedCertificate.remove.msg', 'PKJ', 'Removing this trusted certificate could break a trust chain.'),
            fn: function (state) {
                if (state === 'confirm') {

                    Ext.Ajax.request({
                        url: '/api/pir/truststores/' + me.currentTrustStoreId + '/certificates/' + certificateRecord.get('id'),
                        method: 'DELETE',
                        callback: function (config, success, response) {
                            if (Ext.isEmpty(response.responseText)) {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.trustedCertificateRemoved', 'PKJ', 'Trusted certificate removed'));
                                me.navigateToTrustStoreAndCertificatesPage();
                            }
                        }
                    });
                }
            }
        });
    },

    downloadCertificate: function(certificateRecord) {
        var url = '/api/pir/truststores/' + this.currentTrustStoreId + '/certificates/' + certificateRecord.get('id') + '/download/certificate';
        window.open(url, '_blank');
    }

});
