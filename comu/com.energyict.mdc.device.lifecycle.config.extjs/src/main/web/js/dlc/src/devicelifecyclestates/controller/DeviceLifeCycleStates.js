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
            'device-life-cycle-state-edit #addOnEntryTransitionBusinessProcess':{
                click: this.addEntryTransitionBusinessProcessesToState
            },
            'device-life-cycle-state-edit #addOnExitTransitionBusinessProcess':{
                click: this.addExitTransitionBusinessProcessesToState
            },
            'device-life-cycle-states-action-menu': {
                show: this.configureMenu
            },
            'AddProcessesToState button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            'AddProcessesToState button[name=add]': {
                click: this.addSelectedProcesses
            }
        });
    },
    editLifeCyclePathArguments: null,
    configureMenu: function (menu) {
        var initialAction = menu.down('#initialAction'),
            isInitial = menu.record.get('isInitial');

        isInitial ? initialAction.hide() : initialAction.show();
    },

    showErrorPanel: function (value) {
        var editForm = this.getLifeCycleStatesEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    },

    saveState: function (btn) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getLifeCycleStatesEditForm(),
            successMessage = Uni.I18n.translate('deviceLifeCycleStates.saved', 'DLC', 'State saved'),
            record;

        editForm.updateRecord();
        record = editForm.getRecord();

        me.showErrorPanel(false);
        editForm.setLoading();

        if (btn.action === 'add') {
            successMessage = Uni.I18n.translate('deviceLifeCycleStates.added', 'DLC', 'State added');
        }

        record.save({
            success: function () {
                var route;
                if (me.fromAddTransition) {
                    route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add');
                } else if (me.fromEditTransition) {
                    route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/edit');
                } else {
                    route = router.getRoute('administration/devicelifecycles/devicelifecycle/states');
                }
                route.forward();
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
            }
        });
    },

    cancelAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        if (me.fromAddTransition) {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add');
        } else if (me.fromEditTransition) {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/edit');
        } else {
            route = router.getRoute('administration/devicelifecycles/devicelifecycle/states');
        }
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
            jsonData: null,
            success: function () {
                router.getRoute().forward(null, router.queryParams);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleStates.saved', 'DLC', 'State saved'));
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
            view;

        store.getProxy().setUrl(router.arguments);

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                view = Ext.widget('device-life-cycle-states-setup', {
                    router: router,
                    lifecycleRecord: deviceLifeCycleRecord
                });
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                view.down('#device-life-cycle-link').setText(deviceLifeCycleRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    showDeviceLifeCycleStatePreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycle-states-preview'),
            previewForm = page.down('device-life-cycle-states-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        preview.down('device-life-cycle-states-action-menu').record = record;
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showDeviceLifeCycleStateEdit: function (deviceLifeCycleId, id) {
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
        if (!Ext.isEmpty(id)) {
            stateModel.load(id, {
                success: function (record) {
                    form.loadRecord(record);
                }
            });
        } else {
            form.loadRecord(Ext.create(stateModel));
        }
        widget.setLoading(false);
    },

    removeState: function () {
        var me = this,
            grid = me.getPage().down('#states-grid'),
            record = grid.getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceLifeCycleStates.remove.msg', 'DLC', 'This state will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DLC', 'Remove') + ' \'' + record.get('name') + '\'?',
            fn: function (state) {
                if (state === 'confirm') {
                    record.getProxy().setUrl(router.arguments);
                    me.getPage().setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleStates.remove.success.msg', 'DLC', 'State removed'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            me.getPage().setLoading(false);
                        }
                    });
                }
            }
        });
    },

    addEntryTransitionBusinessProcessesToState: function (){
        this.addTransitionBusinessProcessesToState('onEntry');
    },

    addExitTransitionBusinessProcessesToState: function () {
        this.addTransitionBusinessProcessesToState('onExit');
    },
    addTransitionBusinessProcessesToState: function (storeToUpdate){
        var router = this.getController('Uni.controller.history.Router');
        this.editLifeCyclePathArguments = router.arguments;
        router.getRoute('administration/devicelifecycles/devicelifecycle/states/edit'+(storeToUpdate === 'onEntry' ? '/addEntryProcesses' : '/addExitProcesses')).forward();
    },
    showAvailableEntryTransitionProcesses: function (){
        this.showAvailableTransitionProcesses('onEntry');
    },
    showAvailableExitTransitionProcesses: function () {
        this.showAvailableTransitionProcesses('onExit');
    },
    showAvailableTransitionProcesses: function(storeToUpdate){
        var router = this.getController('Uni.controller.history.Router'),
            store =  Ext.data.StoreManager.lookup(storeToUpdate),
            widget = Ext.widget('AddProcessesToState');
        if (store){
             widget.storeToUpdate = store;
             widget.exclude(store.data.items);
        }
        this.getApplication().fireEvent('changecontentevent', widget );
    },
    //Just like go back to next page????
    forwardToPreviousPage: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();

        router.getRoute(splittedPath.join('/')).forward();
    },

    addSelectedProcesses: function () {
        var router = this.getController('Uni.controller.history.Router'),
            widget = this.getAddProcessesToState(),
            selection = widget.getSelection();

        if (!Ext.isEmpty(selection)) {
            var store = widget.storeToUpdate;
            console.log(store.storeId +' before :'+  store.count()+' processes set');
            Ext.each(selection, function (transitionBusinessProcess) {
                store.add(transitionBusinessProcess);
            });
            console.log(store.storeId +' after :'+store.count()+' processes set');
        }

        // HELP, I NEED SOMEBODY
        // HELP, NOT JUST ANYBODE
        // HELP, YOU KNOW I NEED SOMEONE, HEEEEELP
        // The Beatles...

        // Is following code correct: I try to go back to the editPage
        // By doing this the controller's showDeviceLifeCycleStateEdit method is called
        // This will reload the state => my changes (added a entry/exit process) are lost ????
        router.arguments = this.editLifeCyclePathArguments;
        router.getRoute('administration/devicelifecycles/devicelifecycle/states/edit').forward();
        // Problem !!!!!!!!!
        // Here I am trying to update the deviceLifeCycle state used as model for the LifecycleStatesEditForm
        // This does not work as getLifeCylceStatesEditForm() returns null ???
        // As I'm not on the editPage => this doesn't work...
        // How do I update the 'model'
        getLifeCycleStatesEditForm().updateRecord(); // This is needed so the model reflects the situation on the screen
    }
});