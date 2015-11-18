Ext.define('Dbp.processes.controller.Processes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Dbp.privileges.DeviceProcesses',
        'Dbp.processes.store.Processes'
    ],
    views: [
        'Dbp.processes.view.Processes',
        'Dbp.processes.view.EditProcess',
        'Dbp.processes.view.AddDeviceStatesSetup',
        'Dbp.processes.view.AddPrivilegesSetup'
    ],
    models: [
        'Dbp.processes.model.Process',
        'Dbp.processes.model.DeviceState',
        'Dbp.processes.model.EditProcess',
        'Dbp.processes.model.Privilege'
    ],
    stores: [
        'Dbp.processes.store.Processes',
        'Dbp.processes.store.Associations',
        'Dbp.processes.store.DeviceStates',
        'Dbp.processes.store.Privileges'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'dbp-processes'
        },
        {
            ref: 'mainGrid',
            selector: 'dbp-processes dbp-processes-grid'
        },
        {
            ref: 'processesForm',
            selector: 'dbp-processes #dbp-processes-form'
        },
        {
            ref: 'deviceStatesGrid',
            selector: 'dbp-edit-process #dbp-device-states-grid'
        },
        {
            ref: 'privilegesGrid',
            selector: 'dbp-edit-process #dbp-privileges-grid'
        },
        {
            ref: 'addDeviceStatesGrid',
            selector: 'dbp-add-device-states-setup #grd-add-device-states'
        },
        {
            ref: 'addPrivilegesGrid',
            selector: 'dbp-add-privileges-setup #grd-add-privileges'
        },
        {
            ref: 'deviceStatesGridPreviewContainer',
            selector: '#device-states-grid-preview-container'
        },
        {
            ref: 'privilegesGridPreviewContainer',
            selector: '#privileges-grid-preview-container'
        }


    ],

    editProcessModel: null,

    init: function () {
        this.control({
            'dbp-processes dbp-processes-grid': {
                select: this.showPreview
            },
            'dbp-process-action-menu': {
                show: this.onMenuShow,
                click: this.chooseAction
            },
            'dbp-device-states-action-menu': {
                click: this.chooseEditProcessAction
            },
            'dbp-privileges-action-menu': {
                click: this.chooseEditProcessAction
            },
            '#dbp-device-states-grid button[action=addDeviceState]': {
                click: this.addDeviceStatesButton
            },
            '#device-state-grid-add-link': {
                click: this.addDeviceStatesButton
            },
            '#dbp-privileges-grid button[action=addPrivileges]': {
                click: this.addPrivilegesButton
            },
            '#privileges-grid-add-link': {
                click: this.addPrivilegesButton
            },
            '#btn-add-deviceStates': {
                click: this.addSelectedDeviceStates
            },
            '#btn-add-privileges': {
                click: this.addSelectedPrivileges
            }
        });
    },

    showProcesses: function () {
        var me = this,
            view;

        view = Ext.widget('dbp-processes', {});
        me.getApplication().fireEvent('onBeforeLoad', view);
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('dbp-process-preview'),
            previewForm = page.down('dbp-process-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('dbp-process-action-menu') && (preview.down('dbp-process-action-menu').record = record);
        Ext.resumeLayouts();
    },

    deactivateProcess: function (record) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            processesForm = me.getProcessesForm();

        record.beginEdit();
        record.set('active', 'INACTIVE');
        record.endEdit(true);

        processesForm.setLoading();
        record.save({
            success: function (rec, operation) {

                if (operation.getResultSet().records) {

                    var retRecord = operation.getResultSet().records[0];
                    processesForm.down('#frm-preview-process').loadRecord(retRecord);
                    if (retRecord.get('active') === 'INACTIVE') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dbp.process.deactivate', 'DBP', 'Process deactivated'));
                    }
                }
            },
            callback: function () {
                processesForm.setLoading(false);
            }
        });
    },

    changeProcessActivation: function (record) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            processesForm = me.getProcessesForm();

        record.beginEdit();
        record.set('active', record.get('active') === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE');
        record.endEdit(true);

        processesForm.setLoading();

        record.save({
            success: function (rec, operation) {

                if (operation.getResultSet().records) {
                    record.beginEdit();
                    record.set('associatedTo', operation.getResultSet().records[0].get('associatedTo'));
                    record.endEdit(true);
                    me.getMainGrid().getView().refresh();
                    processesForm.down('#frm-preview-process').loadRecord(record);
                }

                if (record.get('active') === 'INACTIVE') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dbp.process.deactivate', 'DBP', 'Process deactivated'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dbp.process.activate', 'DBP', 'Process activated'));
                }
            },
            callback: function () {
                processesForm.setLoading(false);
            }
        });

    },

    applyNewState: function (queryString) {
        var me = this,
            href = Uni.util.QueryString.buildHrefWithQueryString(queryString, false);

        if (window.location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.href = href;
            Ext.util.History.currentToken = window.location.hash.substr(1);
        }
    },

    editProcess: function (processId, activate) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
       //     queryString = Uni.util.QueryString.getQueryStringValues(false);

     //   if (!activate && queryString.activate == undefined)  {
     //       queryString.activate = activate;
    //        me.applyNewState(queryString);
    //    }
   //     else{
    //        activate = queryString.activate;
    //    }
        //me.editProcessModel = me.editProcessModel  || me.getModel('Dbp.processes.model.EditProcess');


        //    editProcessModel.load(processId, {
        //        success: function (record) {        {
        {

            var record = me.createDummyProcessData(processId);
            me.getApplication().fireEvent(activate? 'activateProcesses':'editProcesses', record);

            view = Ext.widget('dbp-edit-process', {processId: processId, processModel: record});
            var editProcessForm = view.down('#frm-edit-process');
            if (router.arguments.activate) {
                editProcessForm.down('#device-state-grid-add-link').href = router.getRoute('administration/managementprocesses/activate/deviceStates').buildUrl({processId: processId})
                editProcessForm.down('#privileges-grid-add-link').href = router.getRoute('administration/managementprocesses/activate/privileges').buildUrl({processId: processId})
            }
            else {
                editProcessForm.down('#device-state-grid-add-link').href = router.getRoute('administration/managementprocesses/edit/deviceStates').buildUrl({processId: processId})
                editProcessForm.down('#privileges-grid-add-link').href = router.getRoute('administration/managementprocesses/edit/privileges').buildUrl({processId: processId})

            }

            if (activate) {
                record.set('active', 'ACTIVATE');
                editProcessForm.setTitle(Uni.I18n.translate('editProcess.activate', 'DBP', "Activate '{0}'", record.get('name')));
            }
            else {
                editProcessForm.setTitle(Uni.I18n.translate('editProcess.edit', 'DBP', "Edit '{0}'", record.get('name')));
            }

            editProcessForm.loadRecord(record);

            me.getApplication().fireEvent('changecontentevent', view);

            me.refreshDeviceStatesGrid(record.deviceStates());
            me.refreshPrivilegesGrid(record.privileges());
            me.getDeviceStatesGridPreviewContainer().onChange(me.editProcessModel.deviceStates());
            me.getPrivilegesGridPreviewContainer().onChange(me.editProcessModel.privileges());
        }
        //    });

    },

    refreshDeviceStatesGrid: function (store) {
        var me = this,
            deviceStatesGrid = me.getDeviceStatesGrid();

        //deviceStatesGrid.store = store;
        //deviceStatesGrid.getView().bindStore(store);
        //deviceStatesGrid.down('pagingtoolbartop').store = store;
        //deviceStatesGrid.down('pagingtoolbartop').store.totalCount = store.getCount();
        deviceStatesGrid.down('pagingtoolbartop').displayMsg =
            Uni.I18n.translatePlural('deviceStates.pagingtoolbartop.displayMsg', store.getCount(), 'DBP',
                'No device states', '{0} device state', '{0} device states');
        deviceStatesGrid.down('pagingtoolbartop').updateInfo();
    },

    refreshPrivilegesGrid: function (store) {
        var me = this,
            privilegesGrid = me.getPrivilegesGrid();

        //privilegesGrid.store = store;
        //privilegesGrid.getView().bindStore(store);
        privilegesGrid.down('pagingtoolbartop').store = store;
        privilegesGrid.down('pagingtoolbartop').store.totalCount = store.getCount();
        privilegesGrid.down('pagingtoolbartop').displayMsg =
            Uni.I18n.translatePlural('privileges.pagingtoolbartop.displayMsg', store.getCount(), 'DBP',
                'No privileges', '{0} privilege', '{0} privileges');
        privilegesGrid.down('pagingtoolbartop').updateInfo();
    },

    addDeviceStatesButton: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceStatesGridStore = me.getDeviceStatesGrid().getStore(),
            deviceStates = [];

        deviceStatesGridStore.each(function (record) {
            deviceStates.push(record.get('deviceStateId'));
        });
        if (router.arguments.activate){
            router.getRoute('administration/managementprocesses/activate/deviceStates').forward({processId: router.arguments.processId}, {deviceState: deviceStates});
        }
        else {
            router.getRoute('administration/managementprocesses/edit/deviceStates').forward({processId: router.arguments.processId}, {deviceState: deviceStates});
        }
    },

    addDeviceStates: function (processId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allDeviceStatesForm, addDeviceStatesGrid,
            deviceStatesStore = me.getStore('Dbp.processes.store.DeviceStates'),
            allDeviceStatesView = Ext.create('Dbp.processes.view.AddDeviceStatesSetup', {processId: processId});

        if (router.arguments.activate){
            allDeviceStatesView.down('#btn-cancel-add-deviceStates').href = router.getRoute('administration/managementprocesses/activate').buildUrl({processId: processId});
        }
        else {
            allDeviceStatesView.down('#btn-cancel-add-deviceStates').href = router.getRoute('administration/managementprocesses/edit').buildUrl({processId: processId});
        }

        deviceStatesStore.loadData([], false);
        me.getApplication().fireEvent('changecontentevent', allDeviceStatesView);

        deviceStatesStore.load({
            callback: function (records, operation, success) {
                allDeviceStatesView.setLoading();
                if (router.queryParams && router.queryParams.deviceState) {
                    var deviceStates = router.queryParams.deviceState;
                    if (typeof deviceStates === 'string') {
                        deviceStates = [deviceStates];
                    }
                    deviceStates.forEach(function (deviceState) {
                        var rowIndex = deviceStatesStore.findExact('deviceStateId', parseInt(deviceState));

                        if (rowIndex != -1) {
                            deviceStatesStore.removeAt(rowIndex);
                        }
                    });
                }

                allDeviceStatesView.setLoading(false);
            }
        });
    },

    addSelectedDeviceStates: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (me.editProcessModel == null) {
            //    editProcessModel.load(processId, {
            //        success: function (record) {        {
            {

                me.createDummyProcessData(processId);
                me.mergeDeviceStates();
            }
            //    });
        }
        else {
            me.mergeDeviceStates();
        }
    },

    mergeDeviceStates: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceStatesStore = me.getStore('Dbp.processes.store.DeviceStates'),
            addDeviceStatesGrid = me.getAddDeviceStatesGrid();

        deviceStatesStore.each(function (record) {
            if (addDeviceStatesGrid.getSelectionModel().isSelected(record)) {
                var deviceState = Ext.create('Dbp.processes.model.DeviceState');
                deviceState.set('name', record.get('name'));
                deviceState.set('deviceState', record.get('name'));
                deviceState.set('deviceStateId', record.get('deviceStateId'));
                me.editProcessModel.deviceStates().add(deviceState);
            }
        });
        if (router.arguments.activate){
        router.getRoute('administration/managementprocesses/activate').forward({processId: router.arguments.processId});
        }
        else {
            router.getRoute('administration/managementprocesses/edit').forward({processId: router.arguments.processId});
        }
    },

    addPrivilegesButton: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            privilegesGridStore = me.getPrivilegesGrid().getStore(),
            privileges = [];

        privilegesGridStore.each(function (record) {
            privileges.push(record.get('name'));
        });

        if (router.arguments.activate){
            router.getRoute('administration/managementprocesses/activate/privileges').forward({processId: router.arguments.processId}, {privilege: privileges});
        }
        else {
            router.getRoute('administration/managementprocesses/edit/privileges').forward({processId: router.arguments.processId}, {privilege: privileges});
        }
    },

    addPrivileges: function (processId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allPrivilegesForm, addPrivilegesGrid,
            privilegesStore = me.getStore('Dbp.processes.store.Privileges'),
            allPrivilegesView = Ext.create('Dbp.processes.view.AddPrivilegesSetup', {processId: processId});

        if (router.arguments.activate){
            allPrivilegesView.down('#btn-cancel-add-privileges').href = router.getRoute('administration/managementprocesses/activate').buildUrl({processId: processId});
        }
        else {
            allPrivilegesView.down('#btn-cancel-add-privileges').href = router.getRoute('administration/managementprocesses/edit').buildUrl({processId: processId});
        }
        privilegesStore.loadData([], false);
        me.getApplication().fireEvent('changecontentevent', allPrivilegesView);

        privilegesStore.load({
            callback: function (records, operation, success) {
                allPrivilegesView.setLoading();

                if (router.queryParams && router.queryParams.privilege) {
                    var privileges = router.queryParams.privilege;
                    if (typeof privileges === 'string') {
                        privileges = [privileges];
                    }
                    privileges.forEach(function (privilege) {
                        var rowIndex = privilegesStore.findExact('name', privilege);

                        if (rowIndex != -1) {
                            privilegesStore.removeAt(rowIndex);
                        }
                    });
                }

                //allPrivilegesView.down('#grd-add-privileges').store = privilegesStore;
                //allPrivilegesView.down('#grd-add-privileges').getView().bindStore(privilegesStore);

                allPrivilegesView.setLoading(false);
            }
        });
    },

    addSelectedPrivileges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (me.editProcessModel == null) {
            //    editProcessModel.load(processId, {
            //        success: function (record) {        {
            {

                me.createDummyProcessData(processId);
                me.mergePrivileges();
            }
            //    });
        }
        else {
            me.mergePrivileges();
        }
    },

    mergePrivileges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            privilegesStore = me.getStore('Dbp.processes.store.Privileges'),
            addPrivilegesGrid = me.getAddPrivilegesGrid();

        privilegesStore.each(function (record) {
            if (addPrivilegesGrid.getSelectionModel().isSelected(record)) {
                var privilege = Ext.create('Dbp.processes.model.Privilege');
                privilege.set('name', record.get('name'));
                privilege.set('userRoles', record.get('userRoles'));
                me.editProcessModel.privileges().add(privilege);

            }
        });
        if (router.arguments.activate){
        router.getRoute('administration/managementprocesses/activate').forward({processId: router.arguments.processId});
        }
        else {
            router.getRoute('administration/managementprocesses/edit').forward({processId: router.arguments.processId});
        }
    },

    onMenuShow: function (menu) {
        if (menu.record.get('active') === 'ACTIVE') {
            menu.down('#menu-activate-process').hide();
            menu.down('#menu-edit-process').show();
            menu.down('#menu-deactivate-process').show();
        } else {
            menu.down('#menu-edit-process').hide();
            menu.down('#menu-deactivate-process').hide();
            menu.down('#menu-activate-process').show();
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route, record;

        record = menu.record || me.getMainGrid().getSelectionModel().getLastSelected();
        router.arguments.processId = record.get('id');

        switch (item.action) {
            case 'editProcess':
                me.editProcessModel = null;
                route = 'administration/managementprocesses/edit';
                route && (route = router.getRoute(route));
                route && route.forward(router.arguments);
                break;
            case 'deactivateProcess':
                me.editProcessModel = null;
                me.deactivateProcess(record);
                break;
            case 'activateProcess':
                me.editProcessModel = null;
                route = 'administration/managementprocesses/activate';
                route && (route = router.getRoute(route));
                route && route.forward(router.arguments);
                break;
        }
    },

    chooseEditProcessAction: function (menu, item) {
        var me = this,
           record = menu.record;

        switch (item.action) {
            case 'removeDeviceState':
                me.removeDeviceState(record);
                break;
            case 'removePrivileges':
               me.removePrivileges(record);
                break;
        }
    },

    removeDeviceState: function (record) {
        var me = this;

        me.editProcessModel.deviceStates().remove(record);
        me.refreshDeviceStatesGrid(me.editProcessModel.deviceStates());
    },

    removePrivileges: function (record) {
        var me = this;

        me.editProcessModel.privileges().remove(record);
        me.refreshPrivilegesGrid(me.editProcessModel.privileges());
    },

    createDummyProcessData: function (processId) {
        var me = this;

        if (me.editProcessModel){
            return me.editProcessModel;
        }
        me.editProcessModel = Ext.create('Dbp.processes.model.EditProcess', {
            'id': processId,
            'associatedTo': 'Device',
            'name': 'bpp'
        });

        me.editProcessModel.set('deviceStates', []);
        var deviceStates = me.editProcessModel.deviceStates();
        deviceStates.add({
            'name': 'Device life cycle',
            'deviceState': 'In stock',
            'deviceStateId': 1
        });
        deviceStates.add({
            'name': 'Device life cycle',
            'deviceState': 'Active',
            'deviceStateId': 2
        });
        deviceStates.add({
            'name': 'Custom life cycle',
            'deviceState': 'Custom state',
            'deviceStateId': 3
        });
        me.editProcessModel.set('deviceStates', deviceStates);

        me.editProcessModel.set('privileges', []);
        var privileges = me.editProcessModel.privileges();
        var arr = [];
        var o1 = {};
        o1.name = 'Meter expert';
        arr.push(o1);

        var o2 = {};
        o2.name = 'Colectiv #';
        arr.push(o2);
             privileges.add({
              'name': 'Execute processes (level #1)',
               'userRoles': arr
             });
            me.editProcessModel.set('privileges', privileges);
        return me.editProcessModel;
    }
});