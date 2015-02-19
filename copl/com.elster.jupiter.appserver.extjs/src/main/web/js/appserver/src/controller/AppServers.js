Ext.define('Apr.controller.AppServers', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.appservers.Setup',
        'Apr.view.appservers.Add'
    ],
    stores: [
        'Apr.store.AppServers',
        'Apr.store.ServedMessageServices',
        'Apr.store.UnservedMessageServices',
        'Apr.store.ExportPaths'
    ],
    models: [
        'Apr.model.AppServer',
        'Uni.component.sort.model.Sort',
        'Apr.model.ExportPath'
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
            ref: 'sortingToolbar',
            selector: 'appservers-setup #appservers-sorting-toolbar'
        }
    ],
    appServer: null,
    exportPath: null,

    init: function () {
        this.control({
            'appservers-setup appservers-grid': {
                select: this.showPreview
            },
            'appservers-action-menu': {
                click: this.chooseAction
            },
            'appservers-add #add-message-services-button': {
                click: this.openListOfServices
            },
            'message-services-action-menu': {
                click: this.removeMessageService
            },
            'appservers-add #add-edit-button': {
                click: this.addEditAppServer
            },
            'appservers-add message-services-grid': {
                edit: this.onCellEdit
            }
        });
    },

    showAppServers: function () {
        var me = this,
            view = Ext.widget('appservers-setup'),
            store = view.down('appservers-grid').getStore(),
            exportPathsStore = me.getStore('Apr.store.ExportPaths');

        me.getApplication().fireEvent('changecontentevent', view);
        me.appServer = null;
        me.exportPath = null;
        me.getSortingToolbar().getContainer().add({
            xtype: 'button',
            ui: 'tag',
            text: Uni.I18n.translate('general.name', 'UNI', 'Name'),
            iconCls: 'x-btn-sort-item-desc'
        });
        store.on('load', function () {
            exportPathsStore.load(function (records) {
                Ext.Array.each(store.getRange(), function (item) {
                    Ext.Array.each(records, function (dir) {
                        if (dir.get('appServerName') === item.get('name')) {
                            item.set('exportPath', dir.get('directory'));
                            if (view.down('appservers-grid') && (item.getId() === view.down('appservers-grid').getSelectionModel().getLastSelected().getId())) {
                                view.down('#export-path').setValue(dir.get('directory'));
                            }
                        }
                    });
                });
            });
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('appservers-preview'),
            appServerName = record.get('name'),
            previewForm = page.down('appservers-preview-form');

        preview.setTitle(appServerName);
        previewForm.loadRecord(record);
        preview.down('appservers-action-menu').record = record;
        me.setupMenuItems(record);
    },

    setupMenuItems: function (record) {
        var me = this,
            suspended = record.data.active;
        var textKey = ((suspended == true) ? 'general.deactivate' : 'general.activate'),
            text = ((suspended == true) ? 'Deactivate' : 'Activate'),
            menuItems = Ext.ComponentQuery.query('menu menuitem[action=activateAppServer]');
        if (!Ext.isEmpty(menuItems)) {
            Ext.Array.each(menuItems, function (item) {
                item.setText(Uni.I18n.translate(textKey, 'APR', text));
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
                route = 'administration/appservers/edit';
                break;
            case 'removeAppServer':
                me.removeAppServer(menu.record);
                break;
            case 'activateAppServer':
                me.activateAppServer(menu.record);
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

    activateAppServer: function (record) {
        var me = this,
            suspended = record.data.active;

        var action = ((suspended == true) ? 'deactivate' : 'activate');

        Ext.Ajax.request({
            url: '/api/apr/appserver/' + record.data.name + '/' + action,
            method: 'PUT',
            success: function () {
                var messageKey = ((suspended == true) ? 'appServers.deactivateSuccessMsg' : 'appServers.activateSuccessMsg');
                var messageText = ((suspended == true) ? 'Application server deactivated' : 'Application server activated');
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate(messageKey, 'APR', messageText));
                me.showAppServers();
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

                    var titleKey = ((suspended == true) ? 'appServers.deactivate.operation.failed' : 'appServers.activate.operation.failed'),
                        titleValue = ((suspended == true) ? 'Deactivate operation failed' : 'Activate operation failed');


                    me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate(titleKey, 'APR', titleValue), errorText);
                }
            }
        });
    },

    removeAppServer: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            exportPathModel = me.getModel('Apr.model.ExportPath');

        exportPathModel.load(record.data.name, {
            success: function (exportPath) {
                exportPath.destroy({
                    success: function () {
                        confirmationWindow.show({
                            msg: Uni.I18n.translate('appServers.remove.msg', 'APR', 'This application server will no longer be available.'),
                            title: Uni.I18n.translate('general.remove', 'UNI', 'Remove') + ' ' + record.data.name + '?',
                            fn: function (state) {
                                if (state === 'confirm') {
                                    record.destroy({
                                        success: function () {
                                            var grid = me.getPage().down('appservers-grid');
                                            grid.down('pagingtoolbartop').totalCount = 0;
                                            grid.down('pagingtoolbarbottom').resetPaging();
                                            grid.getStore().load();
                                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.remove.success.msg', 'APR', 'Application server removed'));
                                        }
                                    });
                                } 
                            }
                        });
                    }
                });
            }
        });
    },

    showAddEditAppServer: function (appServerName) {
        var me = this,
            view,
            store,
            edit,
            menu,
            unservedStore = me.getStore('Apr.store.UnservedMessageServices'),
            exportPathsStore = me.getStore('Apr.store.ExportPaths');

        if (appServerName) {
            store = me.getStore('Apr.store.ServedMessageServices');
            store.getProxy().setUrl(appServerName);
            edit = true;
            me.getApplication().fireEvent('appserverload', appServerName);
        } else {
            store = unservedStore;
            store.getProxy().setUrl(null);
            edit = false;
        }
        store.load(function (recs) {
            view = Ext.widget('appservers-add', {
                edit: edit,
                store: store
            });
            menu = view.down('#add-message-services-menu');
            menu.on('beforehide', function () {
                var checkedServices = [];
                Ext.Array.each(menu.query('menucheckitem'), function (menuItem) {
                    if (menuItem.checked) {
                        checkedServices.push(menuItem.record);
                        menu.remove(menuItem);
                    }
                });
                store.add(checkedServices);
                if (!Ext.isEmpty(checkedServices)) {
                    view.down('message-services-grid').show();
                    view.down('#empty-text-grid').hide();
                }
            });
            if (appServerName) {
                if (Ext.isEmpty(recs)) {
                    view.down('message-services-grid').hide();
                    view.down('#empty-text-grid').show();
                }
                view.down('#add-appserver-form').setTitle(Uni.I18n.translate('general.edit', 'APR', 'Edit') + " '" + appServerName + "'");
                view.down('#appserver-name').setValue(appServerName);
                view.down('#appserver-name').disable();
                unservedStore.getProxy().setUrl(appServerName);
                unservedStore.load(function (records) {
                    Ext.Array.each(records, function (service) {
                        me.addMessageServiceItem(service);
                    });
                    me.getModel('Apr.model.AppServer').load(appServerName, {
                        success: function (rec) {
                            me.appServer = rec;
                            exportPathsStore.load(function (exportPaths) {
                                Ext.Array.each(exportPaths, function (dir) {
                                    if (dir.get('appServerName') === appServerName) {
                                        me.exportPath = dir;
                                        view.down('#appserver-path').setValue(dir.get('directory'));
                                    } else {
                                        me.exportPath = Ext.create('Apr.model.ExportPath');
                                    }
                                });
                            });
                        }
                    });
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            } else {
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    openListOfServices: function () {
        if (Ext.isEmpty(this.getAddPage().down('#add-message-services-menu').query('menucheckitem'))) {
            this.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.allMessageServicesAdded', 'APR', 'All message services added'));
        }
    },

    addMessageServiceItem: function (service) {
        this.getAddPage().down('#add-message-services-menu').add(
            {
                xtype: 'menucheckitem',
                text: Uni.I18n.translate(service.get('messageService'), 'APR', service.get('messageService')),
                checked: false,
                record: service
            }
        );
    },

    removeMessageService: function (menu) {
        var me = this,
            grid = me.getAddPage().down('message-services-grid');

        grid.getStore().remove(menu.record);
        if (Ext.isEmpty(grid.getStore().getRange())) {
            me.getAddPage().down('message-services-grid').hide();
            me.getAddPage().down('#empty-text-grid').show();
        }
        me.addMessageServiceItem(menu.record);
    },

    addEditAppServer: function () {
        var me = this,
            form = me.getAddPage().down('#add-appserver-form'),
            appServerName = form.down('#appserver-name').getValue(),
            exportPath = form.down('#appserver-path').getValue(),
            grid = me.getAddPage().down('message-services-grid'),
            formErrorsPanel = form.down('#form-errors'),
            record,
            executionSpecs = [],
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
                } else {
                    subscriberSpec.destination = item.get('destination');
                    subscriberSpec.subscriber = item.get('subscriber');
                    subscriberSpec.displayName = item.get('displayName');
                }
                service.subscriberSpec = subscriberSpec;
                if (item.get('numberOfThreads')) {
                    service.numberOfThreads = item.get('numberOfThreads');
                } else {
                    service.numberOfThreads = 1;
                }
                executionSpecs.push(service);
            });
            record = me.appServer || Ext.create('Apr.model.AppServer');
            record.beginEdit();
            if (!isEdit) {
                record.set('name', appServerName);
                record.set('active', false);
            }
            record.set('executionSpecs', executionSpecs);
            record.endEdit();
            me.getAddPage().setLoading();
            if (!isEdit) {
                record.save({
                    action: 'create',
                    success: function () {
                        var newExportPath = Ext.create('Apr.model.ExportPath');
                        newExportPath.set('appServerName', appServerName);
                        newExportPath.set('directory', exportPath);
                        newExportPath.save({
                            action: 'create',
                            success: function () {
                                me.getController('Uni.controller.history.Router').getRoute('administration/appservers').forward();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.addSuccessMsg', 'APR', 'Application server added'));
                            }
                        });
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
                if (!me.exportPath.get('appServerName')) {
                    me.exportPath.set('appServerName', appServerName);
                }
                me.exportPath.set('directory', exportPath);
                me.exportPath.save({
                    success: function () {
                        record.save({
                            success: function () {
                                me.getController('Uni.controller.history.Router').getRoute('administration/appservers').forward();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.editSuccessMsg', 'APR', 'Application server edited'));
                            },
                            failure: function () {
                                me.getAddPage().setLoading(false);
                            }
                        });
                    },
                    failure: function () {
                        me.getAddPage().setLoading(false);
                    }
                });
            }
        } else {
            formErrorsPanel.show();
        }
    },

    onCellEdit: function (editor, e) {
        e.record.set('numberOfThreads', e.value);
    }
});