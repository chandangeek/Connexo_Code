/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.AppServers', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.appservers.Setup',
        'Apr.view.appservers.Add',
        'Apr.view.appservers.AppServerOverview',
        'Apr.view.appservers.AppServerMessageServices',
        'Apr.view.appservers.AppServerImportServices',
        'Apr.view.appservers.AppServerWebserviceEndpoints',
        'Apr.view.appservers.AddMessageServicesGrid',
        'Apr.view.appservers.AddImportServicesGrid',
        'Apr.view.appservers.AddMessageServicesSetup',
        'Apr.view.appservers.AddImportServicesSetup',
        'Apr.view.appservers.AddWebserviceEndpointsSetup',
        'Apr.view.appservers.ImportServicePreview'
    ],
    stores: [
        'Apr.store.Clipboard',
        'Apr.store.AppServers',
        'Apr.store.ServedMessageServices',
        'Apr.store.UnservedMessageServices',
        'Apr.store.ServedImportServices',
        'Apr.store.UnservedImportServices',
        'Apr.store.ActiveService',
        'Apr.store.UnservedWebserviceEndpoints',
        'Apr.store.ServedWebserviceEndpoints'
    ],
    models: [
        'Apr.model.AppServer',
        'Uni.component.sort.model.Sort',
        'Apr.model.WebserviceEndpoint'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'appservers-setup'
        },
        {
            ref: 'addPage',
            selector: 'appservers-add'
        },
        {
            ref: 'overviewPage',
            selector: 'appserver-overview'
        },
        {
            ref: 'addMessageServicesGrid',
            selector: 'add-message-services-grid'
        },
        {
            ref: 'addImportServicesGrid',
            selector: 'add-import-services-grid'
        },
        {
            ref: 'addWebserviceEndpointsGrid',
            selector: 'add-webservices-grid'
        },
        {
            ref: 'addAppServerForm',
            selector: '#add-appserver-form'
        },
        {
            ref: 'addMessageServicesButton',
            selector: '#add-message-services-button'
        },
        {
            ref: 'addMessageServicesButtonFromDetails',
            selector: '#add-message-services-button-from-details'
        },
        {
            ref: 'messageServicesGrid',
            selector: 'message-services-grid'
        },
        {
            ref: 'importServicesGrid',
            selector: 'apr-import-services-grid'
        },
        {
            ref: 'webservicesGrid',
            selector: 'apr-web-service-endpoints-grid'
        },
        {
            ref: 'addImportServicesButton',
            selector: '#add-import-services-button'
        },
        {
            ref: 'addWebserviceEndpointsButton',
            selector: '#add-webservices-button'
        },
        {
            ref: 'saveSettingsButton',
            selector: '#save-message-services-settings'
        },
        {
            ref: 'noMessageServicesSaveSettingsButton',
            selector: '#apr-no-msg-services-save-settings-btn'
        },
        {
            ref: 'undoSettingsButton',
            selector: '#undo-message-services-settings'
        },
        {
            ref: 'noMessageServicesUndoSettingsButton',
            selector: '#apr-no-msg-services-undo-btn'
        },
        {
            ref: 'addImportServicesButtonFromDetails',
            selector: '#add-import-services-button-from-detail'
        },
        {
            ref: 'addWebServicesButtonFromDetails',
            selector: '#add-webservices-button-from-detail'
        },
        {
            ref: 'saveImportServicesButton',
            selector: '#save-import-services-settings-button'
        },
        {
            ref: 'saveWebServicesButton',
            selector: '#save-webservices-settings-button'
        },
        {
            ref: 'undoImportServicesButton',
            selector: '#undo-import-services-button'
        },
        {
            ref: 'undoWebServicesButton',
            selector: '#undo-webservices-button'
        },
        {
            ref: 'importServicesPage',
            selector: 'appserver-import-services'
        },
        {
            ref: 'webServicesPage',
            selector: 'appserver-webservices'
        },
        {
            ref: 'messageServicesOverview',
            selector: 'appserver-message-services'
        },
        {
            ref: 'noImportServicesSaveSettingsButton',
            selector: '#apr-no-imp-services-save-settings-btn'
        },
        {
            ref: 'noImportServicesUndoSettingsButton',
            selector: '#apr-no-imp-services-undo-btn'
        },
        {
            ref: 'noImportServicesAddButton',
            selector: '#add-import-services-button-from-detail-empty'
        },
        {
            ref: 'noWebServicesSaveSettingsButton',
            selector: '#apr-no-webservices-save-settings-btn'
        },
        {
            ref: 'noWebServicesUndoSettingsButton',
            selector: '#apr-no-webservices-undo-btn'
        },
        {
            ref: 'noWebServicesAddButton',
            selector: '#add-webservices-button-from-detail-empty'
        }
    ],
    appServer: null,
    edit: null,

    init: function () {
        this.control({
            'appservers-setup appservers-grid': {
                select: this.showPreview
            },
            'appservers-action-menu': {
                click: this.chooseAction
            },
            '#add-message-services-button': {
                click: this.showAddMessageServices
            },
            '#add-message-services-button-from-details': {
                click: this.showAddMessageServices
            },
            '#apr-no-msg-services-add-one-btn': {
                click: this.showAddMessageServices
            },
            '#add-import-services-button': {
                click: this.showAddImportServiceView
            },
            '#add-import-services-button-from-detail': {
                click: this.showAddImportServiceView
            },
            '#add-import-services-button-from-detail-empty': {
                click: this.showAddImportServiceView
            },
            '#add-webservices-button': {
                click: this.showAddWebserviceView
            },
            '#add-webservices-button-from-detail': {
                click: this.showAddWebserviceView
            },
            '#add-webservices-button-from-detail-empty': {
                click: this.showAddWebserviceView
            },
            'message-services-action-menu': {
                click: this.removeMessageServiceViaMenu
            },
            'apr-import-services-action-menu': {
                click: this.removeImportServiceFromActionMenu
            },
            'apr-webservices-action-menu': {
                click: this.removeWebserviceFromActionMenu
            },
            'apr-import-services-grid actioncolumn': {
                removeEvent: this.removeImportServiceFromGrid
            },
            'apr-web-service-endpoints-grid actioncolumn': {
                removeEvent: this.removeWebserviceEndpointFromGrid
            },
            'appservers-add #add-edit-button': {
                click: this.addEditAppServer
            },
            'appservers-add message-services-grid': {
                msgServiceRemoveEvent: this.onRemoveMessageService
            },
            '#btn-add-message-services': {
                click: this.addMessageServices
            },
            '#lnk-cancel-add-message-services[action=add]': {
                click: this.returnToAddEditViewWithFakeRouter
            },
            '#lnk-cancel-add-message-services[action=detail]': {
                click: this.returnToMessageServiceDetailView
            },
            '#lnk-cancel-add-import-services': {
                click: this.cancelAddImportServices
            },
            '#btn-add-import-services': {
                click: this.addImportServices
            },
            '#lnk-cancel-add-webservices': {
                click: this.cancelAddWebserviceEndpoints
            },
            '#btn-add-webservices': {
                click: this.addWebserviceEndpoints
            },
            '#save-message-services-settings': {
                click: this.saveMessageServerSettings
            },
            '#apr-no-msg-services-save-settings-btn': {
                click: this.saveMessageServerSettings
            },
            '#undo-message-services-settings': {
                click: this.undoMessageServiceChanges
            },
            '#apr-no-msg-services-undo-btn': {
                click: this.undoMessageServiceChanges
            },
            'appserver-message-services #message-services-grid': {
                edit: this.messageServiceDataChanged,
                select: this.showMessageServicePreview,
                msgServiceRemoveEvent: this.onRemoveMessageService
            },
            'preview-container apr-import-services-grid': {
                select: this.showImportServicesPreview
            },
            'preview-container apr-web-service-endpoints-grid': {
                select: this.showWebServicesPreview
            },
            '#save-import-services-settings-button': {
                click: this.saveImportSettings
            },
            '#apr-no-imp-services-save-settings-btn': {
                click: this.saveImportSettings
            },
            '#save-webservices-settings-button': {
                click: this.saveWebserviceSettings
            },
            '#apr-no-webservices-save-settings-btn': {
                click: this.saveWebserviceSettings
            },
            '#apr-no-imp-services-undo-btn': {
                click: this.undoImportServiceChanges
            },
            '#undo-import-services-button': {
                click: this.undoImportServiceChanges
            },
            '#apr-no-webservices-undo-btn': {
                click: this.undoWebserviceEndpointChanges
            },
            '#undo-webservices-button': {
                click: this.undoWebserviceEndpointChanges
            }

        });
    },

    showAppServers: function () {
        var me = this,
            view = Ext.widget('appservers-setup', {
                router: me.getController('Uni.controller.history.Router')
            });
        me.getApplication().fireEvent('changecontentevent', view);

    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('appservers-preview'),
            appServerName = record.get('name'),
            previewForm = page.down('appservers-preview-form');

        preview.setTitle(appServerName);
        previewForm.updateAppServerPreview(record);
        if (preview.down('appservers-action-menu')) {
            preview.down('appservers-action-menu').record = record;
            me.setupMenuItems(record);
        }
    },

    showAppServerOverview: function (appServerName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('appserver-overview', {
                router: router,
                appServerName: appServerName
            });

        me.getModel('Apr.model.AppServer').load(appServerName, {
            success: function (record) {
                me.appServer = record;
                view.down('appservers-preview-form').updateAppServerPreview(record);
                if (view.down('appservers-action-menu')) {
                    view.down('appservers-action-menu').record = record;
                    me.setupMenuItems(record);
                }
            }
        });

        me.getApplication().fireEvent('changecontentevent', view);
        me.getApplication().fireEvent('appserverload', appServerName);
    },

    showMessageServices: function (appServerName) {
        if (!Uni.util.History.isSuspended()) {
            var me = this,
                view,
                servedMessageServicesStore = me.getStore('Apr.store.ServedMessageServices'),
                unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices');
            servedMessageServicesStore.getProxy().setUrl(appServerName);
            unservedMessageServicesStore.getProxy().setUrl(appServerName);
            servedMessageServicesStore.load(function () {
                view = Ext.widget('appserver-message-services', {
                    router: me.getController('Uni.controller.history.Router'),
                    appServerName: appServerName,
                    store: servedMessageServicesStore
                });
                me.getApplication().fireEvent('appserverload', appServerName);
                me.getApplication().fireEvent('changecontentevent', view);
                view.down('preview-container').updateOnChange(!servedMessageServicesStore.getCount()); // to autoselect the 1st item
                view.down('#apr-msg-service-preview').setVisible(servedMessageServicesStore.getCount());
                me.updateMessageServiceCounter();
                unservedMessageServicesStore.load(function (unservedMessages) {
                    if (unservedMessages.length > 0) {
                        view.down('#add-message-services-button-from-details').enable();
                    }
                    me.getModel('Apr.model.AppServer').load(appServerName, {
                        success: function (record) {
                            me.appServer = record;
                        }
                    });
                });
            });
        }
    },

    showMessageServicePreview: function (selectionModel, record) {
        var me = this,
            overview = me.getMessageServicesOverview(),
            preview = overview.down('msg-service-preview'),
            previewForm = overview.down('msg-service-preview-form'),
            menu = preview.down('message-services-action-menu');

        preview.setTitle(record.get('messageService'));
        previewForm.updatePreview(record);
        if (menu) {
            menu.record = record;
        }
    },

    showImportServices: function (appServerName) {
        if (!Uni.util.History.isSuspended()) {
            var me = this,
                view,
                servedImportStore = me.getStore('Apr.store.ServedImportServices'),
                unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
            servedImportStore.getProxy().setUrl(appServerName);
            unservedImportStore.getProxy().setUrl(appServerName);
            unservedImportStore.load(function () {
                servedImportStore.load(function () {
                    me.getModel('Apr.model.AppServer').load(appServerName, {
                        success: function (record) {
                            me.appServer = record;
                            view = Ext.widget('appserver-import-services', {
                                router: me.getController('Uni.controller.history.Router'),
                                appServerName: appServerName,
                                store: servedImportStore
                            });
                            me.getApplication().fireEvent('appserverload', appServerName);
                            me.getApplication().fireEvent('changecontentevent', view);
                            var disabled = unservedImportStore.getCount() === 0;
                            me.updateImportServiceCounter();
                            me.importServicesDataChanged();
                            view.down('preview-container').updateOnChange(!servedImportStore.getCount());
                            if (me.getAddImportServicesButtonFromDetails()) {
                                me.getAddImportServicesButtonFromDetails().setDisabled(disabled);
                            }
                            if (me.getNoImportServicesAddButton()) {
                                me.getNoImportServicesAddButton().setDisabled(disabled);
                            }

                        }
                    });
                });
            });
        }

    },

    showWebServiceEndpoints: function (appServerName) {
        if (!Uni.util.History.isSuspended()) {
            var me = this,
                view,
                servedWebserviceEnpointsStore = me.getStore('Apr.store.ServedWebserviceEndpoints'),
                unservedWebserviceEndpointsStore = me.getStore('Apr.store.UnservedWebserviceEndpoints');
            servedWebserviceEnpointsStore.getProxy().setUrl(appServerName);
            unservedWebserviceEndpointsStore.getProxy().setUrl(appServerName);
            unservedWebserviceEndpointsStore.load(function () {
                servedWebserviceEnpointsStore.load(function () {
                    me.getModel('Apr.model.AppServer').load(appServerName, {
                        success: function (record) {
                            me.appServer = record;
                            view = Ext.widget('appserver-webservices', {
                                router: me.getController('Uni.controller.history.Router'),
                                appServerName: appServerName,
                                store: servedWebserviceEnpointsStore,
                                needLink: true
                            });
                            me.getApplication().fireEvent('appserverload', appServerName);
                            me.getApplication().fireEvent('changecontentevent', view);
                            var disabled = unservedWebserviceEndpointsStore.getCount() === 0;
                            me.webservicesEndpointsDataChanged();
                            view.down('preview-container').updateOnChange(!servedWebserviceEnpointsStore.getCount());
                            if (me.getAddWebServicesButtonFromDetails()) {
                                me.getAddWebServicesButtonFromDetails().setDisabled(disabled);
                            }
                            if (me.getNoWebServicesAddButton()) {
                                me.getNoWebServicesAddButton().setDisabled(disabled);
                            }

                        }
                    });
                });
            });
        }
    },

    setupMenuItems: function (record) {
        var suspended = record.data.active,
            menuText = suspended
                ? Uni.I18n.translate('general.deactivate', 'APR', 'Deactivate')
                : Uni.I18n.translate('general.activate', 'APR', 'Activate'),
            menuItems = Ext.ComponentQuery.query('menu menuitem[action=activateAppServer]');
        if (!Ext.isEmpty(menuItems)) {
            Ext.Array.each(menuItems, function (item) {
                item.setText(menuText);
            });
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.appServerName = menu.record.get('name');

        switch (item.action) {
            case 'editAppServer':
                router.setState(router.getRoute()); // store the current url
                route = 'administration/appservers/edit';
                break;
            case 'removeAppServer':
                me.removeAppServer(menu.record);
                break;
            case 'activateAppServer':
                router.setState(router.getRoute()); // store the current url
                me.activateAppServer(menu.record);
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

    activateAppServer: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            suspended = record.data.active;

        var action = ((suspended == true) ? 'deactivate' : 'activate');

        Ext.Ajax.request({
            url: '/api/apr/appserver/' + record.data.name + '/' + action,
            method: 'PUT',
            jsonData: Ext.encode(record.raw),
            success: function () {
                var messageText = suspended
                    ? Uni.I18n.translate('appServers.deactivateSuccessMsg', 'APR', 'Application server deactivated')
                    : Uni.I18n.translate('appServers.activateSuccessMsg', 'APR', 'Application server activated');
                me.getApplication().fireEvent('acknowledge', messageText);
                router.getState().forward(); // navigate to the previously stored url
            },
            failure: function (response) {
                if (response.status == 400) {
                    var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                    if (!Ext.isEmpty(response.statusText)) {
                        errorText = response.statusText;
                    }
                    if (!Ext.isEmpty(response.responseText)) {
                        var json = Ext.decode(response.responseText, true);
                        if (json && json.error) {
                            errorText = json.error;
                        }
                    }
                    var titleText = suspended
                        ? Uni.I18n.translate('appServers.deactivate.operation.failed', 'APR', 'Deactivate operation failed')
                        : Uni.I18n.translate('appServers.activate.operation.failed', 'APR', 'Activate operation failed');

                    me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                }
            }
        });
    },

    removeAppServer: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('appServers.remove.msg', 'APR', 'This application server will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'APR', "Remove '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        record.destroy({
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.remove.success.msg', 'APR', 'Application server removed'));
                                me.showAppServers();
                            }
                        });
                    }
                }
            });
    },

    showAddEditAppServer: function (appServerName) {
        if (!Uni.util.History.isSuspended()) {
            var me = this,
                router = me.getController('Uni.controller.history.Router'),
                view,
                servedMessageServicesStore = me.getStore('Apr.store.ServedMessageServices'),
                servedImportStore = me.getStore('Apr.store.ServedImportServices'),
                servedWebserviceEndpointsStore = me.getStore('Apr.store.ServedWebserviceEndpoints'),
                unservedImportStore = me.getStore('Apr.store.UnservedImportServices'),
                unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices'),
                unservedWebserviceEndpointsStore = me.getStore('Apr.store.UnservedWebserviceEndpoints');

            if (appServerName) {
                servedMessageServicesStore.getProxy().setUrl(appServerName);
                servedImportStore.getProxy().setUrl(appServerName);
                servedWebserviceEndpointsStore.getProxy().setUrl(appServerName);
                unservedMessageServicesStore.getProxy().setUrl(appServerName);
                unservedImportStore.getProxy().setUrl(appServerName);
                unservedWebserviceEndpointsStore.getProxy().setUrl(appServerName);
                me.edit = true;
                me.getApplication().fireEvent('appserverload', appServerName);

                unservedMessageServicesStore.load(function (messageServices) {
                    unservedImportStore.load(function (importServices) {
                        unservedWebserviceEndpointsStore.load(function (webserviceEndpoints) {
                            servedMessageServicesStore.load(function (servedMessageServices) {
                                servedImportStore.load(function (servedImportServices) {
                                    servedWebserviceEndpointsStore.load(function (servedWebserviceEndpoints) {
                                        me.getModel('Apr.model.AppServer').load(appServerName, {
                                            success: function (rec) {

                                                view = Ext.widget('appservers-add', {
                                                    edit: me.edit,
                                                    store: servedMessageServicesStore,
                                                    importStore: servedImportStore,
                                                    webserviceStore: servedWebserviceEndpointsStore,
                                                    returnLink: router.getState() && router.getState().hasOwnProperty('buildUrl')
                                                        ? router.getState().buildUrl() // = the previously stored url
                                                        : router.getRoute('administration/appservers/overview').buildUrl({appServerName: appServerName})
                                                });
                                                me.getApplication().fireEvent('changecontentevent', view);
                                                view.down('#add-appserver-form').setTitle(Uni.I18n.translate('general.editx', 'APR', "Edit '{0}'", appServerName));
                                                view.down('#add-appserver-form').loadRecord(rec);
                                                view.down('#txt-appserver-name').disable();
                                                if (unservedMessageServicesStore.getCount() === 0) {
                                                    me.getAddMessageServicesButton().disable();
                                                } else {
                                                    me.getAddMessageServicesButton().enable();
                                                }
                                                me.getAddImportServicesButton().setDisabled(unservedImportStore.getCount() === 0);
                                                me.getAddWebserviceEndpointsButton().setDisabled(unservedWebserviceEndpointsStore.getCount() === 0);
                                                me.changeImportGridVisibility(servedImportStore.getCount() !== 0);
                                                me.changeMessageGridVisibility(servedMessageServicesStore.getCount() !== 0);
                                                me.changeWebservicesGridVisibility(servedWebserviceEndpointsStore.getCount() !== 0);
                                            }
                                        });

                                    });
                                });
                            });
                        });
                    });

                });
            } else {
                servedMessageServicesStore.getProxy().setUrl(null);
                servedMessageServicesStore.removeAll();
                servedImportStore.getProxy().setUrl(null);
                servedImportStore.removeAll();
                servedWebserviceEndpointsStore.getProxy().setUrl(null);
                servedWebserviceEndpointsStore.removeAll();
                unservedMessageServicesStore.getProxy().setUrl(null);
                unservedImportStore.getProxy().setUrl(null);
                unservedWebserviceEndpointsStore.getProxy().setUrl(null);
                me.edit = false;
                unservedMessageServicesStore.load(function (messageServices) {
                    unservedImportStore.load(function (importServices) {
                        unservedWebserviceEndpointsStore.load(function (webServices) {
                            Ext.each(messageServices, function (messageService) {
                                var model = Ext.create('Apr.model.ServedMessageService', {
                                    numberOfThreads: 1,
                                    active: true,
                                    subscriberSpec: messageService.data,
                                    messageService: messageService.data.displayName
                                });
                                servedMessageServicesStore.add(model);
                            });
                            unservedMessageServicesStore.removeAll();
                            Ext.each(importServices, function (importService) {
                                servedImportStore.add(importService);
                            });
                            unservedImportStore.removeAll();
                            Ext.each(webServices, function (webservice) {
                                servedWebserviceEndpointsStore.add(webservice);
                            });
                            unservedWebserviceEndpointsStore.removeAll();
                            view = Ext.widget('appservers-add', {
                                edit: me.edit,
                                store: servedMessageServicesStore,
                                importStore: servedImportStore,
                                webserviceStore: servedWebserviceEndpointsStore
                            });
                            var rec = Ext.create('Apr.model.AppServer');
                            view.down('#add-appserver-form').loadRecord(rec);
                            me.getApplication().fireEvent('changecontentevent', view);
                            if (unservedMessageServicesStore.getCount() === 0) {
                                me.getAddMessageServicesButton().disable();
                            } else {
                                me.getAddMessageServicesButton().enable();
                            }
                            me.getAddImportServicesButton().setDisabled(unservedImportStore.getCount() === 0);
                            me.getAddWebserviceEndpointsButton().setDisabled(unservedWebserviceEndpointsStore.getCount() === 0);
                            me.changeImportGridVisibility(servedImportStore.getCount() !== 0);
                            me.changeMessageGridVisibility(servedMessageServicesStore.getCount() !== 0);
                            me.changeWebservicesGridVisibility(servedWebserviceEndpointsStore.getCount() !== 0);
                        });

                    });
                });
            }
        }
    },

    showAddMessageServices: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addDestinationRoute = router.currentRoute + '/addmessageservices',
            route;

        route = router.getRoute(addDestinationRoute);
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
    },

    showAddMessageServiceView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        if (me.getAddAppServerForm()) {
            this.fromDetail = false;
            var view = Ext.widget('add-message-services-setup');
            me.storeCurrentValues();
            me.getApplication().fireEvent('changecontentevent', view);
            view.down('#lnk-cancel-add-message-services').action = 'add';
        } else {
            router.getRoute(me.removeLastPartOfUrl(router.currentRoute)).forward();
        }
    },

    showAddMessageServiceViewFromDetails: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;
        if (!me.appServer) {
            router.getRoute(me.removeLastPartOfUrl(router.currentRoute)).forward();
        } else {
            me.fromDetail = true;
            view = Ext.widget('add-message-services-setup');
            this.getApplication().fireEvent('changecontentevent', view);
            view.down('#lnk-cancel-add-message-services').action = 'detail';
        }
    },

    addMessageServices: function () {
        var me = this,
            grid = this.getAddMessageServicesGrid();
        Ext.each(grid.getSelectionModel().getSelection(), function (messageServiceToAdd) {
            grid.getStore().remove(messageServiceToAdd);
            var served = me.convertToServedMessageServiceModel(messageServiceToAdd);
            me.getStore('Apr.store.ServedMessageServices').add(served);
        });
        if (me.fromDetail) {
            me.returnToMessageServiceDetailView();
            me.messageServiceDataChanged();
        } else {
            me.returnToAddEditViewWithFakeRouter();
        }
    },

    returnToAddEditViewWithFakeRouter: function () {
        var me = this,
            servedMessageServicesStore = me.getStore('Apr.store.ServedMessageServices'),
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            servedWebserviceEndpointsStore = me.getStore('Apr.store.ServedWebserviceEndpoints'),
            view = Ext.widget('appservers-add', {
                edit: me.edit,
                store: servedMessageServicesStore,
                importStore: servedImportStore,
                webserviceStore: servedWebserviceEndpointsStore
            }),
            router = me.getController('Uni.controller.history.Router'),
            route;
        this.restoreValues();
        var unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices');
        me.getAddMessageServicesButton().setDisabled(unservedMessageServicesStore.getCount() === 0);
        var unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
        me.getAddImportServicesButton().setDisabled(unservedImportStore.getCount() === 0);
        var unservedWebserviceEndpointsStore = me.getStore('Apr.store.UnservedWebserviceEndpoints');
        me.getAddWebserviceEndpointsButton().setDisabled(unservedWebserviceEndpointsStore.getCount() === 0);
        if (Ext.isEmpty(servedMessageServicesStore.getRange())) {
            me.changeMessageGridVisibility(false);
        }
        if (Ext.isEmpty(servedImportStore.getRange())) {
            me.changeImportGridVisibility(false);
        }
        if (Ext.isEmpty(servedWebserviceEndpointsStore.getRange())) {
            me.changeWebservicesGridVisibility(false);
        }
        route = router.getRoute(me.removeLastPartOfUrl(router.currentRoute));
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
        this.getApplication().fireEvent('changecontentevent', view);
    },

    removeLastPartOfUrl: function (url) {
        var to = url.lastIndexOf('/') + 1;
        return url.substring(0, to - 1);
    },

    returnToMessageServiceDetailView: function () {
        var me = this,
            servedMessageServicesStore = this.getStore('Apr.store.ServedMessageServices'),
            unservedMessagesStore = this.getStore('Apr.store.UnservedMessageServices'),
            view,
            router = me.getController('Uni.controller.history.Router'),
            route;
        view = Ext.widget('appserver-message-services', {
            router: router,
            appServerName: me.appServer.get('name'),
            store: servedMessageServicesStore
        });
        view.down('#add-message-services-button-from-details').setDisabled(!unservedMessagesStore.count());
        route = router.getRoute(me.removeLastPartOfUrl(router.currentRoute));
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
        me.getApplication().fireEvent('appserverload', me.appServer.get('name'));
        me.getApplication().fireEvent('changecontentevent', view);
        view.down('preview-container').updateOnChange(!servedMessageServicesStore.getCount()); // to autoselect the 1st item
        me.messageServiceDataChanged();
    },

    storeCurrentValues: function () {
        var clipboard = this.getStore('Apr.store.Clipboard');
        this.getAddAppServerForm().updateRecord();
        clipboard.set('model', this.getAddAppServerForm().getRecord());
    },

    restoreValues: function () {
        var clipboard = this.getStore('Apr.store.Clipboard');
        this.getAddAppServerForm().loadRecord(clipboard.get('model'));
    },

    showAddImportServiceView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addDestinationRoute = router.currentRoute + '/addimportservices',
            route;

        route = router.getRoute(addDestinationRoute);
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
    },

    showAddWebserviceView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addDestinationRoute = router.currentRoute + '/addwebserviceendpoints',
            route;

        route = router.getRoute(addDestinationRoute);
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
    },

    showAddImportServices: function () {

        var me = this,
            view = Ext.widget('add-import-services-setup');
        me.fromDetail = false;
        me.storeCurrentValues();
        me.getApplication().fireEvent('changecontentevent', view);
    },

    addImportServices: function () {
        var me = this,
            grid = this.getAddImportServicesGrid();
        Ext.each(grid.getSelectionModel().getSelection(), function (importServiceToAdd) {
            grid.getStore().remove(importServiceToAdd);
            me.getStore('Apr.store.ServedImportServices').add(importServiceToAdd);
            importServiceToAdd.phantom = true;
        });
        if (me.fromDetail) {
            me.returnToImportServiceDetailView();
            me.importServicesDataChanged();
        } else {
            me.returnToAddEditViewWithFakeRouter();
        }
    },

    cancelAddImportServices: function () {
        var me = this;
        if (me.fromDetail) {
            me.returnToImportServiceDetailView();
            me.importServicesDataChanged();
        } else {
            me.returnToAddEditViewWithFakeRouter();
        }
    },

    showAddWebserviceEndpointsView: function () {
        var me = this,
            view = Ext.widget('add-webservices-setup');
        me.fromDetail = false;
        me.storeCurrentValues();
        me.getApplication().fireEvent('changecontentevent', view);
    },

    addWebserviceEndpoints: function () {
        var me = this,
            grid = this.getAddWebserviceEndpointsGrid();
        Ext.each(grid.getSelectionModel().getSelection(), function (webserviceToAdd) {
            grid.getStore().remove(webserviceToAdd);
            me.getStore('Apr.store.ServedWebserviceEndpoints').add(webserviceToAdd);
            webserviceToAdd.phantom = true;
        });
        if (me.fromDetail) {
            me.returnToWebserviceEndpointsDetailView();
            me.webservicesEndpointsDataChanged();
        } else {
            me.returnToAddEditViewWithFakeRouter();
        }
    },

    cancelAddWebserviceEndpoints: function () {
        var me = this;
        if (me.fromDetail) {
            me.returnToWebserviceEndpointsDetailView();
            me.webservicesEndpointsDataChanged();
        } else {
            me.returnToAddEditViewWithFakeRouter();
        }
    },

    returnToImportServiceDetailView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            servedImportStore = this.getStore('Apr.store.ServedImportServices'),
            view,
            route;
        route = router.getRoute(me.removeLastPartOfUrl(router.currentRoute));
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
        view = Ext.widget('appserver-import-services', {
            router: router,
            appServerName: me.appServer.get('name'),
            store: servedImportStore
        });
        me.getApplication().fireEvent('appserverload', me.appServer.get('name'));
        me.getApplication().fireEvent('changecontentevent', view);
        var disabled = me.getStore('Apr.store.UnservedImportServices').getCount() === 0;
        me.getAddImportServicesButtonFromDetails().setDisabled(disabled);
        view.down('preview-container').updateOnChange(!servedImportStore.getCount());
    },

    showImportServicesPreview: function (selectionModel, record) {
        var me = this,
            page = me.getImportServicesPage(),
            preview = page.down('import-service-preview'),
            importService = record.get('importService'),
            previewForm = page.down('import-service-preview-form');

        preview.setTitle(importService);
        previewForm.updateImportServicePreview(record);
        if (preview.down('apr-import-services-action-menu')) {
            preview.down('apr-import-services-action-menu').record = record;
            me.setupMenuItems(record);
        }
    },

    showWebServicesPreview: function (selectionModel, record) {
        var me = this,
            page = me.getWebServicesPage(),
            preview = page.down('webservice-preview'),
            webServiceName = record.get('name'),
            previewForm = page.down('webservice-preview-form');

        preview.setTitle(Ext.htmlEncode(webServiceName));
        previewForm.updateWebservicePreview(record);
        if (preview.down('apr-webservices-action-menu')) {
            preview.down('apr-webservices-action-menu').record = record;
        }
    },

    returnToWebserviceEndpointsDetailView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            servedWebserviceEndpoints = this.getStore('Apr.store.ServedWebserviceEndpoints'),
            view,
            route;
        route = router.getRoute(me.removeLastPartOfUrl(router.currentRoute));
        Uni.util.History.suspendEventsForNextCall();
        route.forward();
        view = Ext.widget('appserver-webservices', {
            router: router,
            appServerName: me.appServer.get('name'),
            store: servedWebserviceEndpoints,
            needLink: true
        });
        me.getApplication().fireEvent('appserverload', me.appServer.get('name'));
        me.getApplication().fireEvent('changecontentevent', view);
        var disabled = me.getStore('Apr.store.UnservedWebserviceEndpoints').getCount() === 0;
        me.getAddWebServicesButtonFromDetails().setDisabled(disabled);
        view.down('preview-container').updateOnChange(!servedWebserviceEndpoints.getCount());
    },


    onRemoveMessageService: function (event) {
        this.doRemoveMessageService(event);
    },

    removeMessageServiceViaMenu: function (menu) {
        this.doRemoveMessageService(menu.record);
    },

    doRemoveMessageService: function (recordToRemove) {
        var me = this,
            grid = me.getMessageServicesGrid(),
            unserved = this.convertToUnservedMessageServiceModel(recordToRemove),
            unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices');

        grid.getStore().remove(recordToRemove);
        unservedMessageServicesStore.add(unserved);
        if (unservedMessageServicesStore.getCount() === 0) {
            if (me.getAddMessageServicesButton()) {
                me.getAddMessageServicesButton().disable();
            }
            if (me.getAddMessageServicesButtonFromDetails()) {
                me.getAddMessageServicesButtonFromDetails().disable();
            }
        } else {
            if (me.getAddMessageServicesButton()) {
                me.getAddMessageServicesButton().enable();
            }
            if (me.getAddMessageServicesButtonFromDetails()) {
                me.getAddMessageServicesButtonFromDetails().enable();
            }
        }
        if (Ext.isEmpty(grid.getStore().getRange())) {
            me.changeMessageGridVisibility(false);
        }
        me.messageServiceDataChanged();
    },

    convertToUnservedMessageServiceModel: function (record) {
        var converted = Ext.create('Apr.model.UnservedMessageService');
        converted.set('destination', record.get('subscriberSpec').destination);
        converted.set('subscriber', record.get('subscriberSpec').subscriber);
        converted.set('displayName', record.get('subscriberSpec').displayName);
        converted.set('messageService', record.get('subscriberSpec').displayName);
        return converted;
    },

    convertToServedMessageServiceModel: function (record) {
        var converted = Ext.create('Apr.model.ServedMessageService');
        converted.set('active', true);
        converted.set('numberOfThreads', 1);
        converted.set('messageService', record.get('displayName'));
        converted.set('subscriberSpec', {
            destination: record.get('destination'),
            displayName: record.get('displayName'),
            subscriber: record.get('subscriber')
        });
        return converted;
    },

    changeMessageGridVisibility: function (visibility) {
        var me = this;
        if (me.getAddPage()) {
            me.getAddPage().down('message-services-grid').setVisible(visibility);
            me.getAddPage().down('#apr-add-msg-services-push-to-right-component').setVisible(visibility);
            me.getAddPage().down('#empty-text-grid').setVisible(!visibility);
        }
    },

    changeWebservicesGridVisibility: function (visibility) {
        var me = this;
        if (me.getAddPage()) {
            me.getAddPage().down('apr-web-service-endpoints-grid').setVisible(visibility);
            me.getAddPage().down('#apr-add-webservices-push-to-right-component').setVisible(visibility);
            me.getAddPage().down('#webservice-empty-text-grid').setVisible(!visibility);
        }
    },

    removeImportServiceFromActionMenu: function (menu) {
        var me = this;
        me.removeImportService(menu.record);
    },

    removeWebserviceFromActionMenu: function (menu) {
        var me = this;
        me.removeWebserviceEndpoint(menu.record);
    },

    removeImportServiceFromGrid: function (record) {
        var me = this;

        me.removeImportService(record);
    },

    removeImportService: function (removedRecord) {
        var me = this,
            grid = me.getImportServicesGrid();

        grid.getStore().remove(removedRecord);
        var unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
        unservedImportStore.add(removedRecord);

        var disable = unservedImportStore.getCount() === 0;
        if (me.getAddImportServicesButton()) {
            me.getAddImportServicesButton().setDisabled(disable);
            if (Ext.isEmpty(grid.getStore().getRange())) {
                me.changeImportGridVisibility(false);
            }
        }
        if (me.getAddImportServicesButtonFromDetails()) {
            me.importServicesDataChanged();
            me.getAddImportServicesButtonFromDetails().setDisabled(disable);
        }
        if (me.getNoImportServicesAddButton()) {
            me.getNoImportServicesAddButton().setDisabled(disable);
        }
    },

    removeWebserviceEndpointFromGrid: function (record) {
        this.removeWebserviceEndpoint(record);
    },

    removeWebserviceEndpoint: function (removedRecord) {
        var me = this,
            grid = me.getWebservicesGrid();

        grid.getStore().remove(removedRecord);
        var unservedWebservicesStore = me.getStore('Apr.store.UnservedWebserviceEndpoints');
        unservedWebservicesStore.add(removedRecord);

        var disable = unservedWebservicesStore.getCount() === 0;
        if (me.getAddWebserviceEndpointsButton()) {
            me.getAddWebserviceEndpointsButton().setDisabled(disable);
            if (Ext.isEmpty(grid.getStore().getRange())) {
                me.changeWebservicesGridVisibility(false);
            }
        }
        if (me.getAddWebServicesButtonFromDetails()) {
            me.webservicesEndpointsDataChanged();
            me.getAddWebServicesButtonFromDetails().setDisabled(disable);
        }
        if (me.getNoWebServicesAddButton()) {
            me.getNoWebServicesAddButton().setDisabled(disable);
        }
    },

    saveImportSettings: function () {
        var me = this,
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            servedImportServices = servedImportStore.getRange(),
            unservedImportStore = me.getStore('Apr.store.UnservedImportServices'),
            importServices = [],
            record = me.appServer,
            ref = me.getImportServicesGrid();

        Ext.Array.each(servedImportServices, function (item) {
            var importService = {};
            if (!item.get('name')) {
                importService.id = item.data.id;
                importService.name = item.data.name;
            } else {
                importService.id = item.get('id');
                importService.name = item.get('name');
            }
            importServices.push(importService);
        });
        record.beginEdit();
        record.set('importServices', importServices);
        record.endEdit();
        if (!ref.up('preview-container').down('no-items-found-panel').isHidden()) {
            ref = ref.up('appserver-import-services');
        }
        ref.setLoading();
        record.save({
            success: function () {
                unservedImportStore.load(function () {
                    servedImportStore.load({
                        callback: function (records, operation, success) {
                            if (success === true) {
                                me.importServicesDataChanged();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.configureImportServicesSuccess', 'APR', 'Import services saved'));
                            }
                            ref.setLoading(false);
                        }
                    });
                });
                me.appServer = record;
                me.importServicesDataChanged();
            },
            failure: function (record, operation) {
                ref.setLoading(false);
                var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                var titleText = Uni.I18n.translate('appServers.save.operation.failed', 'APR', 'Save operation failed');
                me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
            }
        });
    },

    saveWebserviceSettings: function () {
        var me = this,
            servedWebservicesStore = me.getStore('Apr.store.ServedWebserviceEndpoints'),
            servedWebservices = servedWebservicesStore.getRange(),
            unservedWebserviceStore = me.getStore('Apr.store.UnservedWebserviceEndpoints'),
            webServices = [],
            record = me.appServer,
            ref = me.getWebservicesGrid();

        Ext.Array.each(servedWebservices, function (item) {
            var webService = {};
            if (!item.get('name')) {
                webService.id = item.data.id;
                webService.name = item.data.name;
            } else {
                webService.id = item.get('id');
                webService.name = item.get('name');
            }
            webServices.push(webService);
        });
        record.beginEdit();
        record.set('endPointConfigurations', webServices);
        record.endEdit();
        if (!ref.up('preview-container').down('no-items-found-panel').isHidden()) {
            ref = ref.up('appserver-webservices');
        }
        ref.setLoading();
        record.save({
            success: function () {
                unservedWebserviceStore.load(function () {
                    servedWebservicesStore.load({
                        callback: function (records, operation, success) {
                            if (success === true) {
                                me.webservicesEndpointsDataChanged();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.configureWebServicesSuccess', 'APR', 'Web service endpoints saved'));
                            }
                            ref.setLoading(false);
                        }
                    });
                });
                me.appServer = record;
                me.webservicesEndpointsDataChanged();
            },
            failure: function (record, operation) {
                ref.setLoading(false);
                var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                var titleText = Uni.I18n.translate('appServers.save.operation.failed', 'APR', 'Save operation failed');
                me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
            }
        });
    },

    changeImportGridVisibility: function (visibility) {
        var me = this;
        me.getAddPage().down('apr-import-services-grid').setVisible(visibility);
        me.getAddPage().down('#apr-add-imp-services-push-to-right-component').setVisible(visibility);
        me.getAddPage().down('#import-empty-text-grid').setVisible(!visibility);
    },

    addEditAppServer: function () {
        var me = this,
            form = me.getAddPage().down('#add-appserver-form'),
            grid = me.getAddPage().down('message-services-grid'),
            importGrid = me.getAddPage().down('apr-import-services-grid'),
            webservicesGrid = me.getAddPage().down('apr-web-service-endpoints-grid'),
            formErrorsPanel = form.down('#form-errors'),
            record,
            executionSpecs = [],
            importServices = [],
            webServices = [],
            isEdit = me.getAddPage().down('#add-edit-button').action === 'editAppServer';

        if (form.isValid()) {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
            Ext.Array.each(grid.getStore().getRange(), function (item) {
                var service = {},
                    subscriberSpec = {};
                if (!item.get('destination')) {
                    subscriberSpec.destination = item.data.subscriberSpec.destination;
                    subscriberSpec.subscriber = item.data.subscriberSpec.subscriber;
                    subscriberSpec.displayName = item.data.subscriberSpec.displayName;
                }
                service.subscriberSpec = subscriberSpec;
                if (item.get('numberOfThreads')) {
                    service.numberOfThreads = item.get('numberOfThreads');
                } else {
                    service.numberOfThreads = 1;
                }
                service.active = item.get('active');
                executionSpecs.push(service);
            });

            Ext.Array.each(importGrid.getStore().getRange(), function (item) {
                var importService = {};
                if (!item.get('name')) {
                    importService.id = item.data.id;
                    importService.name = item.data.name;
                } else {
                    importService.id = item.get('id');
                    importService.name = item.get('name');
                }
                importServices.push(importService);
            });
            Ext.Array.each(webservicesGrid.getStore().getRange(), function (item) {
                var webService = {};
                if (!item.get('name')) {
                    webService.id = item.data.id;
                    webService.name = item.data.name;
                } else {
                    webService.id = item.get('id');
                    webService.name = item.get('name');
                }
                webServices.push(webService);
            });
            form.updateRecord();
            record = form.getRecord();
            record.beginEdit();
            if (!isEdit) {
                record.set('active', false);
            }
            record.set('executionSpecs', executionSpecs);
            record.set('importServices', importServices);
            record.set('endPointConfigurations', webServices);
            record.endEdit();
            me.getAddPage().setLoading();
            record.save({
                success: function () {
                    me.getAddPage().setLoading(false);
                    me.getController('Uni.controller.history.Router').getRoute('administration/appservers').forward();
                    if (!isEdit) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.addSuccessMsg', 'APR', 'Application server added'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.saveSuccessMsg', 'APR', 'Application server saved'));
                    }

                },
                failure: function (record, operation) {
                    me.getAddPage().setLoading(false);
                    formErrorsPanel.show();
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    saveMessageServerSettings: function () {
        var me = this,
            executionSpecs = [],
            record = me.appServer;
        Ext.Array.each(me.getStore('Apr.store.ServedMessageServices').getRange(), function (item) {
            var service = {},
                subscriberSpec = {};
            if (!item.get('destination')) {
                subscriberSpec.destination = item.data.subscriberSpec.destination;
                subscriberSpec.subscriber = item.data.subscriberSpec.subscriber;
                subscriberSpec.displayName = item.data.subscriberSpec.displayName;
            }
            service.subscriberSpec = subscriberSpec;
            if (item.get('numberOfThreads')) {
                service.numberOfThreads = item.get('numberOfThreads');
            } else {
                service.numberOfThreads = 1;
            }
            service.active = item.get('active');
            executionSpecs.push(service);
        });
        record.beginEdit();
        record.set('executionSpecs', executionSpecs);
        record.endEdit();
        record.save({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute('administration/appservers/overview/messageservices').forward({appServerName: record.get('name')});
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.saveSuccessMsg', 'APR', 'Application server saved'));
            },
            failure: function (response, operation) {
                var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                var titleText = Uni.I18n.translate('appServers.save.operation.failed', 'APR', 'Save operation failed');
                me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
            }
        });
    },

    undoMessageServiceChanges: function () {
        this.showMessageServices(this.getMessageServicesOverview().appServerName);
    },

    onCellEdit: function (editor, e) {
        e.record.set('numberOfThreads', e.value);
    },

    messageServiceDataChanged: function () {
        if (!this.getSaveSettingsButton() && !this.getNoMessageServicesSaveSettingsButton()) { // We're not @ the details view
            return; // ... so nothing to update
        }

        var me = this,
            store = me.getMessageServicesGrid().getStore(),
            storeIsModified = store.getUpdatedRecords().length
                || store.getNewRecords().length
                || store.getRemovedRecords().length;

        if (me.getSaveSettingsButton()) {
            me.getSaveSettingsButton().setDisabled(!storeIsModified);
        }
        if (me.getNoMessageServicesSaveSettingsButton()) {
            me.getNoMessageServicesSaveSettingsButton().setDisabled(!storeIsModified);
        }
        if (me.getUndoSettingsButton()) {
            me.getUndoSettingsButton().setDisabled(!storeIsModified);
        }
        if (me.getNoMessageServicesUndoSettingsButton()) {
            me.getNoMessageServicesUndoSettingsButton().setDisabled(!storeIsModified);
        }
        me.updateMessageServiceCounter();
    },

    importServicesDataChanged: function () {
        var me = this,
            store = me.getImportServicesGrid().getStore(),
            itemsAdded = store.getNewRecords().length > 0,
            itemsRemoved = store.getRemovedRecords().length > 0,
            disable = !(itemsAdded || itemsRemoved);
        if (me.getSaveImportServicesButton()) {
            me.getSaveImportServicesButton().setDisabled(disable);
        }
        if (me.getNoImportServicesUndoSettingsButton()) {
            me.getNoImportServicesUndoSettingsButton().setDisabled(disable);
        }
        if (me.getNoImportServicesSaveSettingsButton()) {
            me.getNoImportServicesSaveSettingsButton().setDisabled(disable);
        }
        if (me.getUndoImportServicesButton()) {
            me.getUndoImportServicesButton().setDisabled(disable);
        }
        me.updateImportServiceCounter();
    },

    webservicesEndpointsDataChanged: function () {
        var me = this,
            store = me.getWebservicesGrid().getStore(),
            itemsAdded = store.getNewRecords().length > 0,
            itemsRemoved = store.getRemovedRecords().length > 0,
            disable = !(itemsAdded || itemsRemoved);
        if (me.getSaveWebServicesButton()) {
            me.getSaveWebServicesButton().setDisabled(disable);
        }
        if (me.getNoWebServicesUndoSettingsButton()) {
            me.getNoWebServicesUndoSettingsButton().setDisabled(disable);
        }
        if (me.getNoWebServicesSaveSettingsButton()) {
            me.getNoWebServicesSaveSettingsButton().setDisabled(disable);
        }
        if (me.getUndoWebServicesButton()) {
            me.getUndoWebServicesButton().setDisabled(disable);
        }
        me.updateWebserviceCounter();
    },

    updateMessageServiceCounter: function () {
        var me = this;
        me.getMessageServicesGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.messageServicesCount', me.getMessageServicesGrid().getStore().getCount(), 'APR', 'No message services', '{0} message service', '{0} message services')
        );
    },

    updateImportServiceCounter: function () {
        var me = this;
        me.getImportServicesGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.importServicesCount', me.getImportServicesGrid().getStore().getCount(), 'APR', 'No import services', '{0} import service', '{0} import services')
        );
    },

    updateWebserviceCounter: function () {
        var me = this;
        me.getWebservicesGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.webserviceEndpointsCount', me.getWebservicesGrid().getStore().getCount(), 'APR', 'No web service endpoints', '{0} web service endpoint', '{0} web service endpoints')
        );
    },

    undoImportServiceChanges: function () {
        var me = this,
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            unservedImportStore = me.getStore('Apr.store.UnservedImportServices');

        unservedImportStore.load(function () {
            servedImportStore.load({
                callback: function (records, operation, success) {
                    if (success === true) {
                        me.importServicesDataChanged();
                        var disabled = unservedImportStore.getCount() === 0;
                        me.getAddImportServicesButtonFromDetails().setDisabled(disabled);
                    } else {
                        var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                        var titleText = Uni.I18n.translate('appServers.undo.operation.failed', 'APR', 'Undo operation failed');
                        me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                    }
                }
            });
        });
    },

    undoWebserviceEndpointChanges: function () {
        var me = this,
            servedWebservicesStore = me.getStore('Apr.store.ServedWebserviceEndpoints'),
            unservedWebservicesStore = me.getStore('Apr.store.UnservedWebserviceEndpoints');

        unservedWebservicesStore.load(function () {
            servedWebservicesStore.load({
                callback: function (records, operation, success) {
                    if (success === true) {
                        me.webservicesEndpointsDataChanged();
                        var disabled = unservedWebservicesStore.getCount() === 0;
                        me.getAddWebServicesButtonFromDetails().setDisabled(disabled);
                    } else {
                        var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                        var titleText = Uni.I18n.translate('appServers.undo.operation.failed', 'APR', 'Undo operation failed');
                        me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                    }
                }
            });
        });
    },

    returnToShowImportServicesIfRefresh: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;
        if (!me.appServer) {
            router.getRoute(me.removeLastPartOfUrl(router.currentRoute)).forward();
        } else {
            view = Ext.widget('add-import-services-setup', {
                router: router,
                appServerName: me.appServer.get('name')
            });
            me.fromDetail = true;
            me.getApplication().fireEvent('changecontentevent', view);
        }
    },

    returnToShowWebserviceEndpointsIfRefresh: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;
        if (!me.appServer) {
            router.getRoute(me.removeLastPartOfUrl(router.currentRoute)).forward();
        } else {
            view = Ext.widget('add-webservices-setup', {
                router: router,
                appServerName: me.appServer.get('name')
            });
            me.fromDetail = true;
            me.getApplication().fireEvent('changecontentevent', view);
        }
    }
});