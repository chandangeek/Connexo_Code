/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.controller.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Scs.view.Setup',
        'Scs.view.Landing',
        'Scs.view.SetupOverview',
        'Scs.view.ServiceCallPreviewContainer',
        'Scs.view.PreviewForm',
        'Uni.view.window.Confirmation'
    ],
    stores: [
        'Scs.store.ServiceCalls',
        'Scs.store.Logs',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],
    models: [
        'Scs.model.ServiceCall'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'service-call-preview-container'
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'overviewTabPanel',
            selector: '#service-call-overview-tab'
        },
        {
            ref: 'landingPage',
            selector: 'scs-landing-page'
        },
        {
            ref: 'serviceCallGrid',
            selector: 'servicecalls-grid'
        },
        {
            ref: 'previewActionButton',
            selector: '#previewMenuButton'
        }
    ],

    init: function () {
        this.control({
            'service-call-preview-container servicecalls-grid': {
                select: this.showPreview
            },
            '#service-calls-overview-scs-menu': {
                click: this.chooseAction
            },
            '#scsActionButton scs-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showServiceCalls: function () {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            view;

        store.setProxy({
            type: 'rest',
            url: '/api/scs/servicecalls',
            timeout: 120000,
            reader: {
                type: 'json',
                root: 'serviceCalls'
            }
        });
        view = Ext.widget('servicecalls-setup', {
            router: me.getController('Uni.controller.history.Router'),
            store: store
        });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showServiceCallSpecifications: function () {
        this.showServiceCallOverview(arguments);
    },

    showServiceCallOverview: function (arguments) {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            logStore = Ext.getStore('Scs.store.Logs'),
            view,
            servicecallId = arguments[arguments.length - 1];
        if (servicecallId) {
            store.setProxy({
                type: 'rest',
                url: '/api/scs/servicecalls/' + servicecallId + '/children',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
            me.getModel('Scs.model.ServiceCall').load(servicecallId, {
                success: function (record) {

                    logStore.getProxy().setUrl(servicecallId);
                    var parents = record.get('parents');
                    parents.push({id:record.get('id'),name:record.get('name')});
                    if (record.get('numberOfChildren') > 0) {
                        view = Ext.widget('servicecalls-setup-overview', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: record.get('name'),
                            store: store,
                            breadcrumbs: parents,
                            record: record,
                            activeTab: window.location.href.indexOf('?') > 0 ? 1 : 0
                        });
                        me.setBreadcrumb(parents);
                        var tp = view.down('tabpanel');
                        var page = view.down('scs-landing-page');
                        if (page && tp.getActiveTab().getItemId() === "specifications-tab") {
                            view.down('scs-landing-page').updateLandingPage(record);
                        }

                        Uni.util.History.setSuspended(false);
                    } else {
                        view = Ext.widget('scs-landing-page', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: record.get('name'),
                            record: record
                        });
                        me.setBreadcrumb(parents);
                        view.updateLandingPage(record);
                    }

                    me.getApplication().fireEvent('changecontentevent', view);

                },
                failure: function (record, operation) {
                    view = Ext.widget('errorNotFound');
                    me.getBreadcrumbs().hide();
                    me.getApplication().fireEvent('changecontentevent', view);
                }
            });

        }
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this,
            trail = this.getBreadcrumbs(),
            root = Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('general.workspace', 'SCS', 'Workspace'),
                href: me.getController('Scs.controller.history.ServiceCall').tokenizeShowOverview()
            }),
            parent = Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                href: 'servicecalls'
            }),
            bc;
        root.setChild(parent);
        Ext.each(breadcrumbs, function (item) {
            bc = Ext.create('Uni.model.BreadcrumbItem', {
                key: item.id,
                text: item.name,
                href: '/' + item.id,
                relative: false
            });
            parent.setChild(bc);
            parent = bc;
        });

        trail.setBreadcrumbItem(root);
        trail.setSkipForNextCall(true);
        trail.doLayout();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalls-preview'),
            serviceCallName = record.get('name'),
            previewForm = preview.down('#servicecall-grid-preview-form'),
            menu = me.getPreviewActionButton().menu,
            oneMenuItemVisible = false;

        me.getPreviewActionButton().down('scs-action-menu').record = record;
        menu.items.each(function(menuItem) {
            if (menuItem.visible) {
                oneMenuItemVisible |= menuItem.visible.call(menu);
            }
        });
        oneMenuItemVisible ? me.getPreviewActionButton().show() : me.getPreviewActionButton().hide();
        me.getModel('Scs.model.ServiceCall').load(record.get('id'), {
            success: function (record) {
                previewForm.updatePreview(record);
            },
            failure: function() {
            }
        });
        preview.setTitle(serviceCallName);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'cancel':
                me.cancelServiceCall(menu.record);
        }
    },

    cancelServiceCall: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.yes', 'SCS', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'SCS', 'No')
            }),
            store = Ext.getStore('Scs.store.ServiceCalls'),
            serviceCallState = record.get('state');
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('servicecall.remove.msg', 'SCS', 'This service call will no longer be running. Do you wish to continue?'),
                title: Uni.I18n.translate('general.cancelX', 'SCS', "Cancel '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        serviceCallState.id = "sclc.default.cancelled";
                        record.set('state', serviceCallState);
                        if(record.get('parents') === '') {
                            record.set('parents', [])
                        }
                        if(record.get('children') === '') {
                            record.set('children', [])
                        }
                        if(record.get('targetObject') === '') {
                            record.set('targetObject', null)
                        }
                        if(me.getLandingPage()) {
                            me.getLandingPage().setLoading();
                        }
                        record.save({
                            success: function(newRecord){
                                if(me.getLandingPage()) {
                                    me.getModel('Scs.model.ServiceCall').load(newRecord.get('id'), {
                                        success: function (record) {
                                            me.getLandingPage().updateLandingPage(record);
                                            me.getLandingPage().down('#scsActionButton').disable();
                                        }
                                    });
                                    me.getLandingPage().setLoading(false);
                                }
                                if(me.getServiceCallGrid()) {
                                    store.load();
                                }
                            },
                            failure: function() {
                                if(me.getLandingPage()) {
                                    me.getLandingPage().setLoading(false);
                                }
                            }
                        });
                    }
                }
            });
    }
});