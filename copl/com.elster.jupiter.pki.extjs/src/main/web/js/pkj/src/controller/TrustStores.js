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
        }
    ],

    currentTrustStoreId: undefined,

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
                click: this.onMenuAction
            },
            'button#pkj-certificates-grid-import-certificates': {
                click: this.navigateToImportTrustedCertificates
            },
            'button#pkj-import-certificates-step': {
                click: this.navigateToImportTrustedCertificates
            },
            'button#pkj-trusted-certificate-import-form-import-btn': {
                click: this.importCertificates
            }
        });
    },

    showTrustStores: function() {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('truststores-overview', {router: this.getController('Uni.controller.history.Router')}));
    },

    navigateToTrustStoresOverviewPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/truststores').forward();
    },

    navigateToAddTrustStore: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/truststores/add').forward();
    },

    navigateToEditTrustStore: function(record) {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/truststores/view/edit')
            .forward({trustStoreId: record.get('id')});
    },

    navigateToImportTrustedCertificates: function(buttonClicked) {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/truststores/view/importcertificates')
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
                        ? Uni.I18n.translate('truststores.saveSuccess', 'PKJ', 'trust store saved')
                        : Uni.I18n.translate('truststores.addSuccess', 'PKJ', 'trust store added'));
                    me.navigateToTrustStoresOverviewPage();
                } else {
                    if (responseText && responseText.errors) {
                        errorMessage.show();
                        form.getForm().markInvalid(responseText.errors);
                    }
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

    onMenuAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'editTrustStore':
                me.navigateToEditTrustStore(menu.record);
                break;
            case 'removeTrustStore':
                me.removeTrustStore(menu.record);
                break;
            //case 'download':
            //    me.activateOrDeactivate(menu.record);
            //    break;
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
                returnLink: router.getRoute('administration/truststores').buildUrl()
            });
        view.down('form').loadRecord(trustStoreRecord);
        me.getApplication().fireEvent('changecontentevent', view);
    },

    removeTrustStore: function(trustStoreRecord) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            title: Uni.I18n.translate('general.removeX', 'PKJ', "Remove '{0}'?", trustStoreRecord.get('name')),
            msg: Uni.I18n.translate('truststores.remove.msg', 'PKJ', 'This trust store will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    trustStoreRecord.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.trustStoreRemoved', 'PKJ', 'Trust store removed.'));
                            me.navigateToTrustStoresOverviewPage();
                        }
                    });
                }
            }
        });
    },

    showTrustedCertificates: function(trustStoreId) {
        var me = this,
            model = Ext.ModelManager.getModel('Pkj.model.TrustStore'),
            certificatesStore = Ext.getStore('Pkj.store.TrustedCertificates');

        certificatesStore.getProxy().setExtraParam('trustStoreId', trustStoreId);
        model.load(trustStoreId, {
            success: function (record) {
                me.currentTrustStoreId = trustStoreId;
                certificatesStore.load(function() {
                    me.showCertificatesPage(record, certificatesStore);
                    me.getApplication().fireEvent('trustStoreLoaded', record.get('name'));
                });
            }
        });
    },

    showCertificatesPage: function(trustStoreRecord, certificateStore) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;

        view = Ext.widget('truststores-certificates-view', {store:certificateStore});
        //view.down('form').loadRecord(trustStoreRecord);
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showImportCertificatesPage: function(trustStoreId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = Ext.ModelManager.getModel('Pkj.model.TrustStore');

        model.load(trustStoreId, {
            success: function (trustStoreRecord) {
                me.getApplication().fireEvent('trustStoreLoaded', trustStoreRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent',
                    Ext.widget('trusted-certificate-import', {
                        cancelLink: router.getRoute('administration/truststores/view').buildUrl({trustStoreId:me.trustStoreId}),
                        trustStoreRecord: trustStoreRecord
                    })
                );
            }
        });
    },

    importCertificates: function() {
        //var me = this,
        //    form = me.getCertificateImportForm(),
        //    errorMsgPanel = form.down('uni-form-error-message'),
        //    record = form.updateRecord().getRecord(),
        //    input = form.down('filefield').button.fileInputEl.dom,
        //    file = input.files[0],
        //    precallback = function (options, success, response) {
        //        if (success) {
        //            callback(options, success, response);
        //        } else {
        //            me.setFormErrors(response, form);
        //            form.setLoading(false);
        //        }
        //    },
        //    callback = function (options, success, response) {
        //        if (success) {
        //            debugger;
        //            //record.doSave(
        //            //    {
        //            //        backUrl: backUrl,
        //            //        callback: me.getOnSaveOptionsCallbackFunction(form, backUrl, 'Firmware version added'))
        //            //    },
        //            //    form
        //            //);
        //        } else {
        //            me.setFormErrors(response, form);
        //            form.setLoading(false);
        //        }
        //    };
        //
        //
        //errorMsgPanel.hide();
        //form.getForm().clearInvalid();
        //if (!form.isValid()) {
        //    errorMsgPanel.show();
        //    return;
        //}
        //
        //if (file) {
        //    form.setLoading();
        //    record.set('keyStoreFileSize', file.size);
        //    record.doValidate(precallback);
        //} else {
        //    record.set('keyStoreFileSize', null);
        //    record.doValidate(precallback);
        //}
    },

    setFormErrors: function (response, form) {
        //form.down('uni-form-error-message').show();
        //var json = Ext.decode(response.responseText);
        //if (json && json.errors) {
        //    var errorsToShow = [];
        //    Ext.each(json.errors, function (item) {
        //        switch (item.id) {
        //            case 'firmwareFileSize':
        //                item.id = 'firmwareFile';
        //                errorsToShow.push(item);
        //                break;
        //            default:
        //                errorsToShow.push(item);
        //                break;
        //        }
        //    });
        //    form.getForm().markInvalid(errorsToShow);
        //}
    }


});
