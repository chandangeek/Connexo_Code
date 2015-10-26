Ext.define('Apr.controller.AppServers', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.appservers.Setup',
        'Apr.view.appservers.Add',
        'Apr.view.appservers.AppServerOverview',
        'Apr.view.appservers.AppServerMessageServices',
        'Apr.view.appservers.AppServerImportServices',
        'Apr.view.appservers.AddMessageServicesGrid',
        'Apr.view.appservers.AddImportServicesGrid',
        'Apr.view.appservers.AddMessageServicesSetup',
        'Apr.view.appservers.AddImportServicesSetup',
        'Apr.view.appservers.ImportServicePreview'
    ],
    stores: [
        'Apr.store.AppServers',
        'Apr.store.ServedMessageServices',
        'Apr.store.UnservedMessageServices',
        'Apr.store.ServedImportServices',
        'Apr.store.UnservedImportServices',
        'Apr.store.ExportPaths',
        'Apr.store.ImportPaths',
        'Apr.store.ActiveService'
    ],
    models: [
        'Apr.model.AppServer',
        'Uni.component.sort.model.Sort',
        'Apr.model.ExportPath',
        'Apr.model.ImportPath'
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
            ref: 'addImportServicesButton',
            selector: '#add-import-services-button'
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
            ref: 'saveImportServicesButton',
            selector: '#save-import-services-settings-button'
        },
        {
            ref: 'importServicesPage',
            selector: 'appserver-import-services'
        },
        {
            ref: 'messageServicesOverview',
            selector: 'appserver-message-services'
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
                click: this.showAddMessageServiceView
            },
            '#add-message-services-button-from-details': {
                click: this.showAddMessageServiceViewFromDetails
            },
            '#apr-no-msg-services-add-one-btn': {
                click: this.showAddMessageServiceViewFromDetails
            },
            '#add-import-services-button': {
                click: this.showAddImportServiceView
            },
            '#add-import-services-button-from-detail': {
                click: this.showAddImportServiceViewFromDetails
            },
            '#add-import-services-button-from-detail-empty': {
                click: this.showAddImportServiceViewFromDetails
            },
            'message-services-action-menu': {
                click: this.removeMessageServiceViaMenu
            },
            'apr-import-services-action-menu': {
                click: this.removeImportService
            },
            'appservers-add #add-edit-button': {
                click: this.addEditAppServer
            },
            'appservers-add message-services-grid': {
                edit: this.onCellEdit,
                msgServiceRemoveEvent: this.onRemoveMessageService
            },
            '#btn-add-message-services': {
                click: this.addMessageServices
            },
            '#lnk-cancel-add-message-services[action=add]': {
                click: this.returnToAddEditViewWithoutRouter
            },
            '#lnk-cancel-add-message-services[action=detail]': {
                click: this.returnToMessageServiceDetailView
            },
            '#lnk-cancel-add-import-services': {
                click: this.cancelAddImportServices
            },
            '#btn-add-import-services':{
                click: this.addImportServices
            },
            '#save-message-services-settings':{
                click: this.saveMessageServerSettings
            },
            '#apr-no-msg-services-save-settings-btn': {
                click: this.saveMessageServerSettings
            },
            '#undo-message-services-settings':{
                click: this.undoMessageServiceChanges
            },
            '#apr-no-msg-services-undo-btn': {
                click: this.undoMessageServiceChanges
            },
            'appserver-message-services #message-services-grid':{
                edit: this.messageServiceDataChanged,
                select: this.showMessageServicePreview,
                msgServiceRemoveEvent: this.onRemoveMessageService
            },
            '#save-import-services-settings-button': {
                click: this.saveImportSettings
            },
            'preview-container apr-import-services-grid': {
                select: this.showImportServicesPreview
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
                if(unservedMessages.length === 0){
                    view.down('#add-message-services-button-from-details').disable();
                }
                me.getModel('Apr.model.AppServer').load(appServerName, {
                    success: function (record) {
                        me.appServer = record;
                    }
                });
            });
        });
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
        var me = this,
            view,
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
            servedImportStore.getProxy().setUrl(appServerName);
            unservedImportStore.getProxy().setUrl(appServerName);
            unservedImportStore.load(function () {
               servedImportStore.load( function () {
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
                         view.down('preview-container').updateOnChange(!servedImportStore.getCount())
                         me.getAddImportServicesButtonFromDetails().setDisabled(disabled);

                     }
                  });
               });
            });
    },

    setupMenuItems: function (record) {
        var me = this,
            suspended = record.data.active,
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
            success: function () {
                var messageText = suspended
                    ? Uni.I18n.translate('appServers.deactivateSuccessMsg', 'APR', 'Application server deactivated')
                    : Uni.I18n.translate('appServers.activateSuccessMsg', 'APR', 'Application server activated');
                me.getApplication().fireEvent('acknowledge', messageText);
                router.getState().forward(); // navigate to the previously stored url
            },
            failure: function (response, request) {
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
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view,
            servedMessageServicesStore = me.getStore('Apr.store.ServedMessageServices'),
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            unservedImportStore = me.getStore('Apr.store.UnservedImportServices'),
            unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices');

        if (appServerName) {
            servedMessageServicesStore.getProxy().setUrl(appServerName);
            servedImportStore.getProxy().setUrl(appServerName);
            unservedMessageServicesStore.getProxy().setUrl(appServerName);
            unservedImportStore.getProxy().setUrl(appServerName);
            me.edit = true;
            me.getApplication().fireEvent('appserverload', appServerName);

            unservedMessageServicesStore.load(function (messageServices) {
                unservedImportStore.load(function (importServices) {
                    servedMessageServicesStore.load(function (servedMessageServices) {
                        servedImportStore.load(function (servedImportServices) {
                            me.getModel('Apr.model.AppServer').load(appServerName, {
                                success: function (rec) {
                                    view = Ext.widget('appservers-add', {
                                        edit: me.edit,
                                        store: servedMessageServicesStore,
                                        importStore: servedImportStore,
                                        returnLink: router.getState() && router.getState().hasOwnProperty('buildUrl')
                                            ? router.getState().buildUrl() // = the previously stored url
                                            : router.getRoute('administration/appservers/overview').buildUrl({appServerName:appServerName})
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
                                    me.changeImportGridVisibility(servedImportStore.getCount() !== 0);
                                    me.changeMessageGridVisibility(servedMessageServicesStore.getCount() !== 0);
                                }
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
            unservedMessageServicesStore.getProxy().setUrl(null);
            unservedImportStore.getProxy().setUrl(null);
            me.edit = false;
            unservedMessageServicesStore.load(function (messageServices) {
                unservedImportStore.load(function (importServices) {
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
                    view = Ext.widget('appservers-add', {
                        edit: me.edit,
                        store: servedMessageServicesStore,
                        importStore: servedImportStore
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
                    me.changeImportGridVisibility(servedImportStore.getCount() !== 0);
                    me.changeMessageGridVisibility(servedMessageServicesStore.getCount() !== 0);
                });

            });
        }
    },

    showAddMessageServiceView: function () {
        this.fromDetail = false;
        var view = Ext.widget('add-message-services-setup');
        this.storeCurrentValues();
        this.getApplication().fireEvent('changecontentevent', view);
        view.down('#lnk-cancel-add-message-services').action = 'add';
    },

    showAddMessageServiceViewFromDetails: function () {
        this.fromDetail = true;
        var view = Ext.widget('add-message-services-setup');
        this.getApplication().fireEvent('changecontentevent', view);
        view.down('#lnk-cancel-add-message-services').action = 'detail';
    },

    addMessageServices: function () {
        var me = this,
            grid = this.getAddMessageServicesGrid();
        Ext.each(grid.getSelectionModel().getSelection(), function (messageServiceToAdd) {
            grid.getStore().remove(messageServiceToAdd);
            var served = me.convertToServedMessageServiceModel(messageServiceToAdd);
            me.getStore('Apr.store.ServedMessageServices').add(served);
        });
        if(this.fromDetail){
            me.returnToMessageServiceDetailView();
            me.messageServiceDataChanged();
        } else {
            me.returnToAddEditViewWithoutRouter();
        }
    },

    returnToAddEditViewWithoutRouter: function () {
        var me = this,
            servedMessageServicesStore = me.getStore('Apr.store.ServedMessageServices'),
            servedImportStore = me.getStore('Apr.store.ServedImportServices'),
            view = Ext.widget('appservers-add', {
                edit: me.edit,
                store: servedMessageServicesStore,
                importStore: servedImportStore
            });
        this.restoreValues();
        this.getApplication().fireEvent('changecontentevent', view);
        var unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
        var unservedMessageServicesStore = me.getStore('Apr.store.UnservedMessageServices');
        if(unservedMessageServicesStore.getCount() === 0){
            me.getAddMessageServicesButton().disable();
        } else {
            me.getAddMessageServicesButton().enable();
        }
        me.getAddImportServicesButton().setDisabled(unservedImportStore.getCount() === 0);
    },

    returnToMessageServiceDetailView: function () {
        var me = this,
            servedMessageServicesStore = this.getStore('Apr.store.ServedMessageServices'),
            unservedMessagesStore = this.getStore('Apr.store.UnservedMessageServices'),
            view;
        view = Ext.widget('appserver-message-services', {
            router: me.getController('Uni.controller.history.Router'),
            appServerName: me.appServer.get('name'),
            store: servedMessageServicesStore
        });
        if(unservedMessagesStore.count() === 0){
            view.down('#add-message-services-button-from-details').disable();
        }
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
            view = Ext.widget('add-import-services-setup');
        me.fromDetail = false;
        me.storeCurrentValues();
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showAddImportServiceViewFromDetails: function () {
        var me = this,
            view = Ext.widget('add-import-services-setup');
        me.fromDetail = true;
        this.getApplication().fireEvent('changecontentevent', view);
    },

    addImportServices: function () {
        var me = this,
            grid = this.getAddImportServicesGrid();
        Ext.each(grid.getSelectionModel().getSelection(),function(importServiceToAdd){
            grid.getStore().remove(importServiceToAdd);
            me.getStore('Apr.store.ServedImportServices').add(importServiceToAdd);
        });
        if(me.fromDetail){
            me.returnToImportServiceDetailView();
            me.getSaveImportServicesButton().enable();
        } else {
            me.returnToAddEditViewWithoutRouter();
        }
    },

    cancelAddImportServices: function () {
        var me = this;
        if(me.fromDetail){
            me.returnToImportServiceDetailView();
        } else {
            me.returnToAddEditViewWithoutRouter();
        }
    },

    returnToImportServiceDetailView: function () {
        var me = this,
            servedImportStore = this.getStore('Apr.store.ServedImportServices'),
            view;
        view = Ext.widget('appserver-import-services', {
            router: me.getController('Uni.controller.history.Router'),
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

    onRemoveMessageService: function (event) {
        this.doRemoveMessageService(event);
    },

    removeMessageServiceViaMenu: function (menu) {
        this.doRemoveMessageService(menu.record);
    },

    doRemoveMessageService: function(recordToRemove) {
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

    removeImportService: function (menu) {
        var me = this,
            grid = me.getImportServicesGrid(),
            removedRecord = menu.record;

        grid.getStore().remove(removedRecord);
        var unservedImportStore = me.getStore('Apr.store.UnservedImportServices');
        unservedImportStore.add(removedRecord);

        var disable = unservedImportStore.getCount() === 0;
        if (me.getAddImportServicesButton()) {
            me.getAddImportServicesButton().setDisabled(disable);
            if (Ext.isEmpty(grid.getStore().getRange())) {
                me.changeImportGridVisibility(false);
            }
        } else if (me.getAddImportServicesButtonFromDetails()) {
            me.getSaveImportServicesButton().enable();
            me.getAddImportServicesButtonFromDetails().enable();
        }
    },

    saveImportSettings: function () {
        var me = this,
            servedImportServices= this.getStore('Apr.store.ServedImportServices').getRange(),
            importServices = [],
            record = me.appServer;

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
        me.getImportServicesGrid().setLoading();
        record.save({
            success: function () {
                me.getImportServicesGrid().setLoading(false);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.configureImportServicesSuccess', 'APR', 'Import services saved'));
                me.appServer = record;
                me.getSaveImportServicesButton().disable()
            },
            failure: function (record, operation) {
                me.getImportServicesGrid().setLoading(false);
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
            formErrorsPanel = form.down('#form-errors'),
            record,
            executionSpecs = [],
            importServices = [],
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
            form.updateRecord();
            record = form.getRecord();
            record.beginEdit();
            if (!isEdit) {
                record.set('active', false);
            }
            record.set('executionSpecs', executionSpecs);
            record.set('importServices', importServices);
            record.endEdit();
            me.getAddPage().setLoading();
            record.save({
                success: function () {
                    me.getAddPage().setLoading(false);
                    me.getController('Uni.controller.history.Router').getRoute('administration/appservers').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.addSuccessMsg', 'APR', 'Application server added'));
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

    saveMessageServerSettings: function(){
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
            failure: function (record, operation) {
                me.getAddPage().setLoading(false);
                formErrorsPanel.show();
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                }
            }
        });
    },

    undoMessageServiceChanges: function(){
        this.showMessageServices(this.getMessageServicesOverview().appServerName);
    },

    onCellEdit: function (editor, e) {
        e.record.set('numberOfThreads', e.value);
    },

    messageServiceDataChanged: function(){
        if (!this.getSaveSettingsButton() && !this.getNoMessageServicesSaveSettingsButton()) { // We're not @ the details view
            return; // ... so nothing to update
        }

        var me =this,
            store = me.getMessageServicesGrid().getStore(),
            itemsUpdated = store.getUpdatedRecords().length > 0,
            itemsAdded = store.getNewRecords().length > 0,
            itemsRemoved = store.getRemovedRecords().length > 0;

        if (itemsUpdated || itemsAdded || itemsRemoved) {
            if (me.getSaveSettingsButton()) {
                me.getSaveSettingsButton().enable();
            }
            if (me.getNoMessageServicesSaveSettingsButton()) {
                me.getNoMessageServicesSaveSettingsButton().enable();
            }
            if (me.getUndoSettingsButton()) {
                me.getUndoSettingsButton().enable();
            }
            if (me.getNoMessageServicesUndoSettingsButton()) {
                me.getNoMessageServicesUndoSettingsButton().enable();
            }
        }
        me.updateMessageServiceCounter();
    },

    updateMessageServiceCounter: function() {
        var me =this;
        me.getMessageServicesGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.messageServicesCount', me.getMessageServicesGrid().getStore().getCount(), 'APR', 'No message services', '{0} message service', '{0} message services')
        );
    }
});