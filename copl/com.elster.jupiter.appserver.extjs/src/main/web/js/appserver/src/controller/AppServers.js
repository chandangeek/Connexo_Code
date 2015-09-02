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
        'Apr.store.ServedImportServices',
        'Apr.store.UnservedImportServices',
        'Apr.store.ExportPaths',
        'Apr.store.ImportPaths'
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

    ],
    appServer: null,
    exportPath: null,
    importPath: null,

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
            'appservers-add #add-import-services-button': {
                click: this.openListOfImportServices
            },
            'message-services-action-menu': {
                click: this.removeMessageService
            },
            'apr-import-services-action-menu': {
                click: this.removeImportService
            },
            'appservers-add #add-edit-button': {
                click: this.addEditAppServer
            },
            'appservers-add message-services-grid': {
                edit: this.onCellEdit
            },
            'appservers-add apr-import-services-grid': {
                edit: this.onCellEdit
            }

        });
    },

    showAppServers: function () {
        var me = this,
            view = Ext.widget('appservers-setup'),
            store = view.down('appservers-grid').getStore(),
            exportPathsStore = me.getStore('Apr.store.ExportPaths'),
            importPathsStore = me.getStore('Apr.store.ImportPaths');

        me.getApplication().fireEvent('changecontentevent', view);
        me.appServer = null;
        me.exportPath = null;
        me.importPath = null;

        store.on('load', function () {
            exportPathsStore.load(function (records) {
                Ext.Array.each(store.getRange(), function (item) {
                    Ext.Array.each(records, function (dir) {
                        if (dir.get('appServerName') === item.get('name')) {
                            item.set('exportPath', dir.get('directory'));
                            if (view.down('appservers-grid') && (item.getId() === view.down('appservers-grid').getSelectionModel().getLastSelected().getId())) {
                                view.down('#txt-export-path').setValue(dir.get('directory'));
                            }
                        }
                    });
                });
            });
            importPathsStore.load(function (records) {
                Ext.Array.each(store.getRange(), function (item) {
                    Ext.Array.each(records, function (dir) {
                        if (dir.get('appServerName') === item.get('name')) {
                            item.set('importPath', dir.get('directory'));
                            if (view.down('appservers-grid') && (item.getId() === view.down('appservers-grid').getSelectionModel().getLastSelected().getId())) {
                                view.down('#txt-import-path').setValue(dir.get('directory'));
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
        previewForm.updateAppServerPreview(record);
        if(preview.down('appservers-action-menu')) {
            preview.down('appservers-action-menu').record = record;
            me.setupMenuItems(record);
        }
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
                confirmationWindow.show({
                    msg: Uni.I18n.translate('appServers.remove.msg', 'APR', 'This application server will no longer be available.'),
                    title: Uni.I18n.translate('general.remove', 'APR', "Remove '{0}'?",[record.data.name]),
                    fn: function (state) {
                        if (state === 'confirm') {
                            exportPath.destroy({
                                success: function () {
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
                            });
                        }
                    }
                });
            }
        });
    },

    showAddEditAppServer: function (appServerName) {
        var me = this,
            view,
            store,
            importStore,
            unservedImportStore = me.getStore('Apr.store.UnservedImportServices'),
            edit,
            menu,
            importMenu,
            unservedStore = me.getStore('Apr.store.UnservedMessageServices'),
            exportPathsStore = me.getStore('Apr.store.ExportPaths'),
            importPathsStore = me.getStore('Apr.store.ImportPaths');

        if (appServerName) {
            store = me.getStore('Apr.store.ServedMessageServices');
            store.getProxy().setUrl(appServerName);

            importStore = me.getStore('Apr.store.ServedImportServices');
            importStore.getProxy().setUrl(appServerName);

            edit = true;
            me.getApplication().fireEvent('appserverload', appServerName);
        } else {
            store = unservedStore;
            store.getProxy().setUrl(null);

            importStore = unservedImportStore;
            importStore.getProxy().setUrl(null);
            edit = false;
        }
        store.load(function (recs) {
            view = Ext.widget('appservers-add', {
                edit: edit,
                store: store,
                importStore: importStore
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
            importStore.load(function (recs){
                importMenu = view.down('#add-import-services-menu');
                importMenu.on('beforehide', function () {
                        var checkedImportServices = [];
                        Ext.Array.each(importMenu.query('menucheckitem'), function (menuItem) {
                            if (menuItem.checked) {
                                checkedImportServices.push(menuItem.record);
                                importMenu.remove(menuItem);
                            }
                        });
                        importStore.add(checkedImportServices);
                    if (!Ext.isEmpty(checkedImportServices)) {
                        view.down('apr-import-services-grid').show();
                        view.down('#import-empty-text-grid').hide();
                    }
                });

                    if (appServerName) {
                        if (Ext.isEmpty(recs)) {
                            view.down('apr-import-services-grid').hide();
                            view.down('#import-empty-text-grid').show();
                        }

                        unservedImportStore.getProxy().setUrl(appServerName);
                        unservedImportStore.load(function (records) {
                            Ext.Array.each(records, function (service) {
                                me.addImportServiceItem(service);
                            });
                        });

                    }
                }
            );
            if (appServerName) {
                if (Ext.isEmpty(recs)) {
                    view.down('message-services-grid').hide();
                    view.down('#empty-text-grid').show();
                }
                view.down('#add-appserver-form').setTitle(Uni.I18n.translate('general.editx', 'APR', "Edit '{0}'",[appServerName]));
                view.down('#txt-appserver-name').setValue(appServerName);
                view.down('#txt-appserver-name').disable();
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
                                        view.down('#appserver-export-path').setValue(dir.get('directory'));
                                    } else {
                                        me.exportPath = Ext.create('Apr.model.ExportPath');
                                    }
                                });
                            });
                            importPathsStore.load(function (importPaths) {
                                Ext.Array.each(importPaths, function (dir) {
                                    if (dir.get('appServerName') === appServerName) {
                                        me.importPath = dir;
                                        view.down('#appserver-import-path').setValue(dir.get('directory'));
                                    } else {
                                        me.importPath = Ext.create('Apr.model.ImportPath');
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

    openListOfImportServices: function () {
        if (Ext.isEmpty(this.getAddPage().down('#add-import-services-menu').query('menucheckitem'))) {
            this.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.allImportServicesAdded', 'APR', 'All import services added'));
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

    addImportServiceItem: function (service) {
        this.getAddPage().down('#add-import-services-menu').add(
            {
                xtype: 'menucheckitem',
                text: service.get('importService'),
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

    addImportServiceItemToMenu: function (service) {
        this.getAddPage().down('#add-import-services-menu').add(
            {
                xtype: 'menucheckitem',
                text: service.get('importService'),
                checked: false,
                record: service
            }
        );
    },

    removeImportService: function (menu) {
        var me = this,
            grid = me.getAddPage().down('apr-import-services-grid');

        grid.getStore().remove(menu.record);
        if (Ext.isEmpty(grid.getStore().getRange())) {
            me.getAddPage().down('apr-import-services-grid').hide();
            me.getAddPage().down('#import-empty-text-grid').show();
        }
        me.addImportServiceItemToMenu(menu.record);
    },
    addEditAppServer: function () {
        var me = this,
            form = me.getAddPage().down('#add-appserver-form'),
            appServerName = form.down('#txt-appserver-name').getValue(),
            exportPath = form.down('#appserver-export-path').getValue(),
            importPath = form.down('#appserver-import-path').getValue(),
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

            record = me.appServer || Ext.create('Apr.model.AppServer');
            record.beginEdit();
            if (!isEdit) {
                record.set('name', appServerName);
                record.set('active', false);
            }
            record.set('executionSpecs', executionSpecs);
            record.set('importServices', importServices);
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
                        var newImportPath = Ext.create('Apr.model.ImportPath');
                        newImportPath.set('appServerName', appServerName);
                        newImportPath.set('directory', importPath);
                        newImportPath.save();
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
                if (me.importPath == null){
                    me.importPath = Ext.create('Apr.model.ImportPath');
                }

                if (!me.importPath.get('appServerName')) {
                    me.importPath.set('appServerName', appServerName);
                }

                me.importPath.set('directory', importPath);
                me.importPath.save();

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