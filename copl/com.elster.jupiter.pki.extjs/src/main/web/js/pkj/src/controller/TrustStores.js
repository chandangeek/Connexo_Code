/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.TrustStores', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.TrustStoresOverview',
        'Pkj.view.AddEditTrustStore',
        'Uni.view.window.Confirmation'
    ],
    stores: [
        'Pkj.store.TrustStores'
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
        }
    ],

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
            }
        });
    },

    showTrustStores: function() {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('truststores-overview'));
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
    }

});
