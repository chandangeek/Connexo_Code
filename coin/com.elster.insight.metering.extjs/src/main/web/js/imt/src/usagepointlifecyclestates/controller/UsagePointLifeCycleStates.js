/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.usagepointlifecyclestates.view.Setup',
        'Imt.usagepointlifecyclestates.view.Edit',
        'Imt.usagepointlifecyclestates.view.AddProcessesToState'
    ],

    stores: [
        'Imt.usagepointlifecyclestates.store.UsagePointLifeCycleStates',
        'Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses',
        'Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnEntry',
        'Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnExit',
        'Imt.usagepointlifecycle.store.Stages'
    ],

    models: [
        'Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState',
        'Imt.usagepointlifecycle.model.UsagePointLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usagepoint-life-cycle-states-setup'
        },
        {
            ref: 'editPage',
            selector: 'usagepoint-life-cycle-state-edit'
        },
        {
            ref: 'lifeCycleStatesGrid',
            selector: 'usagepoint-life-cycle-states-grid'
        },
        {
            ref: 'lifeCycleStatesEditForm',
            selector: '#lifeCycleStateEditForm'
        },
        {
            ref: 'addProcessesToState',
            selector: 'add-processes-to-state'
        }
    ],
    usagePointLifeCycleState: null,
    init: function () {
        this.control({
            'usagepoint-life-cycle-states-setup usagepoint-life-cycle-states-grid': {
                select: this.showUsagePointLifeCycleStatePreview
            },
            'usagepoint-life-cycle-states-setup button[action=addState]': {
                click: this.moveToCreatePage
            },
            'usagepoint-life-cycle-states-action-menu menuitem[action=edit]': {
                click: this.moveToEditPage
            },
            'usagepoint-life-cycle-states-action-menu menuitem[action=setAsInitial]': {
                click: this.setAsInitial
            },
            'usagepoint-life-cycle-states-action-menu menuitem[action=removeState]': {
                click: this.removeState
            },
            'usagepoint-life-cycle-state-edit button[action=cancelAction]': {
                click: this.cancelAction
            },
            'usagepoint-life-cycle-state-edit #createEditButton': {
                click: this.saveState
            },
            'usagepoint-life-cycle-state-edit #addOnEntryTransitionBusinessProcess': {
                click: this.addEntryTransitionBusinessProcessesToState
            },
            'usagepoint-life-cycle-state-edit #addOnExitTransitionBusinessProcess': {
                click: this.addExitTransitionBusinessProcessesToState
            },
            'add-processes-to-state button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            'add-processes-to-state button[name=add]': {
                click: this.addSelectedProcesses
            },
            'add-processes-to-state grid': {
                selectionchange: this.hideProcessesErrorPanel
            }
        });
    },

    showErrorPanel: function (value) {
        var editForm = this.getLifeCycleStatesEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    },

    getProcessItemsFromStore: function (store) {
        var processItems = [];

        store.each(function (record) {
            processItems.push(record.getData());
        });
        return processItems;
    },

    saveState: function (btn) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editForm = me.getLifeCycleStatesEditForm(),
            successMessage = btn.action === 'add'
                ? Uni.I18n.translate('usagePointLifeCycleStates.added', 'IMT', 'State added')
                : Uni.I18n.translate('usagePointLifeCycleStates.saved', 'IMT', 'State saved'),
            entryProcessesStore = editForm.down('#processesOnEntryGrid').getStore(),
            exitProcessesStore = editForm.down('#processesOnExitGrid').getStore(),
            backUrl,
            record;

        editForm.updateRecord();
        record = editForm.getRecord();
        Ext.suspendLayouts();
        me.showErrorPanel(false);
        Ext.resumeLayouts(true);
        editForm.setLoading();
        record.beginEdit();
        record.set('onEntry', me.getProcessItemsFromStore(entryProcessesStore));
        record.set('onExit', me.getProcessItemsFromStore(exitProcessesStore));
        record.endEdit();
        if (me.fromAddTransition) {
            backUrl = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/add').buildUrl();
        } else if (me.fromEditTransition) {
            backUrl = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/edit').buildUrl();
        } else {
            backUrl = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/states').buildUrl();
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
                    Ext.suspendLayouts();
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                editForm.setLoading(false);
                me.usagePointLifeCycleState = null;
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

        editForm.setLoading();
        if (me.fromAddTransition) {
            route = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/add');
        } else if (me.fromEditTransition) {
            route = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions/edit');
        } else {
            route = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/states');
        }
        entryProcessesStore.removeAll();
        exitProcessesStore.removeAll();
        me.usagePointLifeCycleState = null;
        route.forward();
    },

    moveToCreatePage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/usagepointlifecycles/usagepointlifecycle/states/add').forward();
    },

    setAsInitial: function () {
        var me = this,
            page = this.getPage(),
            router = this.getController('Uni.controller.history.Router'),
            record = this.getLifeCycleStatesGrid().getSelectionModel().getLastSelected(),
            state = Ext.create('Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState', record.getData());

        page.setLoading();
        state.setAsInitial(router.arguments.usagePointLifeCycleId, {
            isNotEdit: true,
            success: function () {
                router.getRoute().forward(null, router.queryParams);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycleStates.saved', 'IMT', 'State saved'));
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

        router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/states/edit').forward({id: record.get('id')});
    },

    showUsagePointLifeCycleStates: function (usagePointLifeCycleId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Imt.usagepointlifecyclestates.store.UsagePointLifeCycleStates'),
            entryProcessesStore = me.getStore('Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnEntry'),
            exitProcessesStore = me.getStore('Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnExit'),
            dependenciesCounter = 2,
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    var widget = Ext.widget('usagepoint-life-cycle-states-setup', {
                        router: router,
                        lifecycleRecord: usagePointLifeCycleRecord
                    });

                    if (entryProcessesStore) {
                        entryProcessesStore.removeAll();
                    }
                    if (exitProcessesStore) {
                        exitProcessesStore.removeAll();
                    }

                    widget.down('#usagepoint-life-cycle-link').setText(usagePointLifeCycleRecord.get('name'));
                    me.getApplication().fireEvent('changecontentevent', widget);
                    mainView.setLoading(false);
                }
            },
            usagePointLifeCycleRecord;

        store.getProxy().setUrl(router.arguments);
        mainView.setLoading();
        me.getStore('Imt.usagepointlifecycle.store.Stages').load(onDependenciesLoad);
        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (record) {
                usagePointLifeCycleRecord = record;
                me.getApplication().fireEvent('usagepointlifecycleload', record);
                onDependenciesLoad();
            }
        });
    },

    showUsagePointLifeCycleStatePreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('usagepoint-life-cycle-states-preview'),
            previewForm = page.down('usagepoint-life-cycle-states-preview-form'),
            menu = preview.down('usagepoint-life-cycle-states-action-menu'),
            processesOnEntry = '',
            processesOnExit = '';

        Ext.suspendLayouts();
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (menu) {
            menu.record = record;
        }
        previewForm.loadRecord(record);
        previewForm.down('#entry-container').removeAll();
        previewForm.down('#exit-container').removeAll();
        Ext.Array.each(record.get('onEntry'), function (process) {
            processesOnEntry += Ext.String.htmlEncode(process.name) + '<br/>';
        });
        Ext.Array.each(record.get('onExit'), function (process) {
            processesOnExit += Ext.String.htmlEncode(process.name) + '<br/>';
        });
        previewForm.down('#entry-container').add({
            xtype: 'displayfield',
            htmlEncode: false,
            value: processesOnEntry
        });
        previewForm.down('#exit-container').add({
            xtype: 'displayfield',
            htmlEncode: false,
            value: processesOnExit
        });
        Ext.resumeLayouts(true);
    },

    showUsagePointLifeCycleStateEdit: function (usagePointLifeCycleId, stateId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('usagepoint-life-cycle-state-edit'),
            stateModel = me.getModel('Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState'),
            router = me.getController('Uni.controller.history.Router'),
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            form = widget.down('#lifeCycleStateEditForm'),
            dependenciesCounter = 2,
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    form.loadRecord(me.usagePointLifeCycleState);
                    me.getApplication().fireEvent('changecontentevent', widget);
                    mainView.setLoading(false);
                }
            };

        mainView.setLoading();

        me.fromEditTransition = router.queryParams.fromEditTransitionPage === 'true';
        me.fromAddTransition = router.queryParams.fromEditTransitionPage === 'false';
        stateModel.getProxy().setUrl(router.arguments);

        if (!Ext.isEmpty(stateId)) {
            dependenciesCounter++;
            stateModel.load(stateId, {
                success: function (record) {
                    me.usagePointLifeCycleState = record;
                    me.getApplication().fireEvent('loadlifecyclestate', record);
                    onDependenciesLoad();
                }
            });
        } else {
            me.usagePointLifeCycleState = Ext.create(stateModel);
        }

        me.getStore('Imt.usagepointlifecycle.store.Stages').load(onDependenciesLoad);
        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (record) {
                me.getApplication().fireEvent('usagepointlifecycleload', record);
                onDependenciesLoad();
            }
        });
    },

    removeState: function () {
        var me = this,
            grid = me.getPage().down('#states-grid'),
            record = grid.getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router'),
            page = me.getPage();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('usagePointLifeCycleStates.remove.msg', 'IMT', 'This state will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?", [record.get('name')]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.getProxy().setUrl(router.arguments);
                    page.setLoading(Uni.I18n.translate('general.removing', 'IMT', 'Removing...'));
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycleStates.remove.success.msg', 'IMT', 'State removed'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            page.setLoading(false);
                        }
                    });
                }
            }
        });
        me.usagePointLifeCycleState = null;
    },

    addEntryTransitionBusinessProcessesToState: function () {
        this.addTransitionBusinessProcessesToState('onEntry');
    },

    addExitTransitionBusinessProcessesToState: function () {
        this.addTransitionBusinessProcessesToState('onExit');
    },

    addTransitionBusinessProcessesToState: function (storeToUpdate) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editForm = me.getLifeCycleStatesEditForm();

        editForm.updateRecord();
        router.getRoute(router.currentRoute + (storeToUpdate === 'onEntry' ? '/addEntryProcesses' : '/addExitProcesses')).forward();
    },

    showAvailableEntryTransitionProcesses: function () {
        this.showAvailableTransitionProcesses('Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnEntry');
    },

    showAvailableExitTransitionProcesses: function () {
        this.showAvailableTransitionProcesses('Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnExit');
    },

    showAvailableTransitionProcesses: function (storeToUpdate) {
        if (!this.usagePointLifeCycleState) {
            this.forwardToPreviousPage();
        } else {
            var store = Ext.data.StoreManager.lookup(storeToUpdate),
                widget = Ext.widget('add-processes-to-state', {storeToUpdate: store});

            this.getApplication().fireEvent('changecontentevent', widget);
        }
    },

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
            this.forwardToPreviousPage();
        } else {
            this.showProcessesErrorPanel();
        }
    },

    showProcessesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddProcessesToState().down('#add-process-to-state-errors'),
            errorPanel = me.getAddProcessesToState().down('#add-process-to-state-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideProcessesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddProcessesToState().down('#add-process-to-state-errors'),
            errorPanel = me.getAddProcessesToState().down('#add-process-to-state-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();
    }
});