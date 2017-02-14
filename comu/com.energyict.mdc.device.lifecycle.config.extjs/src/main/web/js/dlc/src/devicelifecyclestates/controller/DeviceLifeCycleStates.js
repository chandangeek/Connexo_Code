/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecyclestates.view.Setup',
        'Dlc.devicelifecyclestates.view.Edit',
        'Dlc.devicelifecyclestates.view.AddProcessesToState'
    ],

    stores: [
        'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates',
        'Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses',
        'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry',
        'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit'
    ],

    models: [
        'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
        'Dlc.devicelifecycles.model.DeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycle-states-setup'
        },
        {
            ref: 'editPage',
            selector: 'device-life-cycle-state-edit'
        },
        {
            ref: 'lifeCycleStatesGrid',
            selector: 'device-life-cycle-states-grid'
        },
        {
            ref: 'lifeCycleStatesEditForm',
            selector: '#lifeCycleStateEditForm'
        },
        {
            ref: 'addProcessesToState',
            selector: 'AddProcessesToState'
        }
    ],
    deviceLifeCycleState: null,
    init: function () {
        this.control({
            'device-life-cycle-states-setup device-life-cycle-states-grid': {
                select: this.showDeviceLifeCycleStatePreview
            },
            'device-life-cycle-states-setup button[action=addState]': {
                click: this.moveToCreatePage
            },
            'device-life-cycle-states-action-menu menuitem[action=edit]': {
                click: this.moveToEditPage
            },
            'device-life-cycle-states-action-menu menuitem[action=setAsInitial]': {
                click: this.setAsInitial
            },
            'device-life-cycle-states-action-menu menuitem[action=removeState]': {
                click: this.removeState
            },
            'device-life-cycle-state-edit button[action=cancelAction]': {
                click: this.cancelAction
            },
            'device-life-cycle-state-edit #createEditButton': {
                click: this.saveState
            },
            'device-life-cycle-state-edit #addOnEntryTransitionBusinessProcess': {
                click: this.addEntryTransitionBusinessProcessesToState
            },
            'device-life-cycle-state-edit #addOnExitTransitionBusinessProcess': {
                click: this.addExitTransitionBusinessProcessesToState
            },
            'AddProcessesToState button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            'AddProcessesToState button[name=add]': {
                click: this.addSelectedProcesses
            }
        });
    },

    showErrorPanel: function (value) {
        var editForm = this.getLifeCycleStatesEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    },

    getProcessItemsFromStore: function(store) {
      var processItems = [];

        store.each(function(record) {
            processItems.push(record.getData());
        });
        return processItems;
    },

    saveState: function (btn) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getLifeCycleStatesEditForm(),
            successMessage = btn.action === 'add'
                ? Uni.I18n.translate('deviceLifeCycleStates.added', 'DLC', 'State added')
                : Uni.I18n.translate('deviceLifeCycleStates.saved', 'DLC', 'State saved'),
            entryProcessesStore = editForm.down('#processesOnEntryGrid').getStore(),
            exitProcessesStore = editForm.down('#processesOnExitGrid').getStore(),
            backUrl,
            record;

        editForm.updateRecord();
        record = editForm.getRecord();
        me.showErrorPanel(false);
        editForm.setLoading();
        record.set('onEntry', me.getProcessItemsFromStore(entryProcessesStore));
        record.set('onExit', me.getProcessItemsFromStore(exitProcessesStore));
        if (me.fromAddTransition) {
            backUrl = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add').buildUrl();
        } else if (me.fromEditTransition) {
            backUrl = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/edit').buildUrl();
        } else {
            backUrl = router.getRoute('administration/devicelifecycles/devicelifecycle/states').buildUrl();
        }

        record.save({
            backUrl: backUrl,
            success: function () {
                entryProcessesStore.removeAll();
                exitProcessesStore.removeAll();
                window.location.href = backUrl;
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function () {
                editForm.setLoading(false);
                me.deviceLifeCycleState = null;
            }
        });
    },

    cancelAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editForm = me.getLifeCycleStatesEditForm(),
            entryProcessesStore = editForm.down('#processesOnEntryGrid').getStore(),
            exitProcessesStore = editForm.down('#processesOnExitGrid').getStore(),
            route;

        if (me.fromAddTransition) {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add');
        } else if (me.fromEditTransition) {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/edit');
        } else {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/states');
        }
        entryProcessesStore.removeAll();
        exitProcessesStore.removeAll();
        me.deviceLifeCycleState = null;
        route.forward();
    },

    moveToCreatePage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicelifecycles/devicelifecycle/states/add').forward();
    },

    setAsInitial: function () {
        var me = this,
            grid = this.getLifeCycleStatesGrid(),
            page = this.getPage(),
            router = this.getController('Uni.controller.history.Router'),
            record = grid.getSelectionModel().getLastSelected();

        page.setLoading();
        Ext.Ajax.request({
            url: '/api/dld/devicelifecycles/' + router.arguments.deviceLifeCycleId + '/states/' + record.get('id') + '/status',
            method: 'PUT',
            jsonData: record.getRecordData(),
            isNotEdit: true,
            success: function () {
                router.getRoute().forward(null, router.queryParams);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleStates.saved', 'DLC', 'State saved'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    moveToEditPage: function () {
        var grid = this.getLifeCycleStatesGrid(),
            router = this.getController('Uni.controller.history.Router'),
            record = grid.getSelectionModel().getLastSelected();

        router.getRoute('administration/devicelifecycles/devicelifecycle/states/edit').forward({id: record.get('id')});
    },

    showDeviceLifeCycleStates: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dlc.devicelifecyclestates.store.DeviceLifeCycleStates'),
            entryProcessesStore = me.getStore('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry'),
            exitProcessesStore = me.getStore('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit'),
            view;

        store.getProxy().setUrl(router.arguments);

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                view = Ext.widget('device-life-cycle-states-setup', {
                    router: router,
                    lifecycleRecord: deviceLifeCycleRecord
                });
                if (entryProcessesStore)
                    entryProcessesStore.removeAll();
                if (exitProcessesStore)
                    exitProcessesStore.removeAll();

                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                view.down('#states-side-menu').setHeader(deviceLifeCycleRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    showDeviceLifeCycleStatePreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycle-states-preview'),
            previewForm = page.down('device-life-cycle-states-preview-form'),
            menu = preview.down('device-life-cycle-states-action-menu');

        Ext.suspendLayouts();
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (menu) {
            menu.record = record;
        }
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showDeviceLifeCycleStateEdit: function (deviceLifeCycleId, stateId) {
        var me = this,
            widget = Ext.widget('device-life-cycle-state-edit'),
            stateModel = me.getModel('Dlc.devicelifecyclestates.model.DeviceLifeCycleState'),
            router = me.getController('Uni.controller.history.Router'),
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            form = widget.down('#lifeCycleStateEditForm');

        me.fromEditTransition = router.queryParams.fromEditTransitionPage === 'true';
        me.fromAddTransition = router.queryParams.fromEditTransitionPage === 'false';
        stateModel.getProxy().setUrl(router.arguments);
        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
            }
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        if (!Ext.isEmpty(stateId)) {
            stateModel.load(stateId, {
                success: function (record) {
                    me.getApplication().fireEvent('loadlifecyclestate', record);
                    form.loadRecord(record);
                }
            });
        } else {
           if (!me.deviceLifeCycleState){
               me.deviceLifeCycleState = Ext.create(stateModel);
           }
           form.loadRecord(me.deviceLifeCycleState);
        }
        widget.setLoading(false);
    },

    removeState: function () {
        var me = this,
            grid = me.getPage().down('#states-grid'),
            record = grid.getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router'),
            page = me.getPage();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceLifeCycleStates.remove.msg', 'DLC', 'This state will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'DLC', "Remove '{0}'?",[record.get('name')]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.getProxy().setUrl(router.arguments);
                    page.setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleStates.remove.success.msg', 'DLC', 'State removed'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            page.setLoading(false);
                        }
                    });
                }
            }
        });
        me.deviceLifeCycleState = null;
    },

    addEntryTransitionBusinessProcessesToState: function () {
        this.addTransitionBusinessProcessesToState('onEntry');
    },

    addExitTransitionBusinessProcessesToState: function () {
        this.addTransitionBusinessProcessesToState('onExit');
    },

    addTransitionBusinessProcessesToState: function (storeToUpdate) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            // save the editForm's current state
            editForm = me.getLifeCycleStatesEditForm();
        editForm.updateRecord();
        router.getRoute(router.currentRoute + (storeToUpdate === 'onEntry' ? '/addEntryProcesses' : '/addExitProcesses')).forward();
    },

    showAvailableEntryTransitionProcesses: function () {
        this.showAvailableTransitionProcesses('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry');
    },

    showAvailableExitTransitionProcesses: function () {
        this.showAvailableTransitionProcesses('Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit');
    },

    showAvailableTransitionProcesses: function (storeToUpdate) {
        var store = Ext.data.StoreManager.lookup(storeToUpdate),
            widget = Ext.widget('AddProcessesToState', {storeToUpdate: store});

        this.getApplication().fireEvent('changecontentevent', widget);
    },

    //Just like 'go back to next page' ????
    forwardToPreviousPage: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    },

    addSelectedProcesses: function () {
        var widget = this.getAddProcessesToState(),
            selection = widget.getSelection();

        if (!Ext.isEmpty(selection)) {
            var store = widget.storeToUpdate;
            Ext.each(selection, function (transitionBusinessProcess) {
                store.add(transitionBusinessProcess);
            });
        }
        this.forwardToPreviousPage();
    }
});