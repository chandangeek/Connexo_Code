/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
            ref: 'editProcessPage',
            selector: 'dbp-edit-process'
        },
        {
            ref: 'editProcessForm',
            selector: '#frm-edit-process'
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
        },
        {
            ref: 'addDeviceStatePage',
            selector: 'dbp-add-device-states-setup'
        },
        {
            ref: 'addPrivilegesPage',
            selector: 'dbp-add-privileges-setup'
        }
    ],

    editProcessRecord: null,

    init: function () {
        this.control({
            'dbp-processes dbp-processes-grid': {
                select: this.showPreview
            },
            'dbp-process-action-menu': {
                show: this.onMenuShow,
                click: this.chooseAction
            },
            '#dbp-device-states-grid button[action=addDeviceState]': {
                click: this.addDeviceStatesButton
            },
            '#dbp-privileges-grid button[action=addPrivileges]': {
                click: this.addPrivilegesButton
            },
            '#add-device-states-button': {
                click: this.addDeviceStatesButton
            },
            '#add-privileges-button': {
                click: this.addPrivilegesButton
            },
            '#btn-add-deviceStates': {
                click: this.addSelectedDeviceStates
            },
            '#btn-add-privileges': {
                click: this.addSelectedPrivileges
            },
            '#dbp-device-states-grid': {
                msgRemoveDeviceState: this.removeDeviceState
            },
            '#dbp-privileges-grid': {
                msgRemovePrivilege: this.removePrivileges
            },
            '#frm-edit-process #btn-save': {
                click: this.saveProcess
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
                    retRecord.set('deploymentId', rec.get('deploymentId'));
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

    editProcess: function (name, version, activate) {
        var me = this,
            editProcessModel;

        if (me.editProcessRecord != null &&
            (name != me.editProcessRecord.get('name') || version != me.editProcessRecord.get('version'))) {
            me.editProcessRecord = null;
        }

        if (me.editProcessRecord == null) {
            editProcessModel = me.getModel('Dbp.processes.model.EditProcess');
            editProcessModel.getProxy().extraParams = {version: version};
            editProcessModel.load(name, {
                success: function (record) {
                    me.loadEditProcess(name, version, activate, record);
                }
            });
        }
        else {
            me.loadEditProcess(name, version, activate, me.editProcessRecord);
        }
    },

    loadEditProcess: function (name, version, activate, record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('dbp-edit-process', {editProcessRecord: record});

        me.getApplication().fireEvent(activate ? 'activateProcesses' : 'editProcesses', name + ':' + version);
        var editProcessForm = view.down('#frm-edit-process');

        editProcessForm.down('#add-device-states-button').href = router.arguments.activate ?
            router.getRoute('administration/managementprocesses/activate/deviceStates').buildUrl({
                name: name,
                version: version
            }) :
            router.getRoute('administration/managementprocesses/edit/deviceStates').buildUrl({
                name: name,
                version: version
            });

        editProcessForm.down('#add-privileges-button').href = router.arguments.activate ?
            router.getRoute('administration/managementprocesses/activate/privileges').buildUrl({
                name: name,
                version: version
            }) :
            router.getRoute('administration/managementprocesses/edit/privileges').buildUrl({
                name: name,
                version: version
            });

        if (activate) {
            record.set('name', name);
            record.set('version', version);
            record.set('active', 'ACTIVE');
            editProcessForm.setTitle(Uni.I18n.translate('editProcess.activate', 'DBP', "Activate '{0}'", name + ':' + version));
        }
        else {
            editProcessForm.setTitle(Uni.I18n.translate('editProcess.edit', 'DBP', "Edit '{0}'", name + ':' + version));
        }

        if (record.get('associatedTo').length == 0) {
            record.beginEdit();
            record.set('associatedTo', 'Device');
            record.endEdit();
        }

        editProcessForm.loadRecord(record);
        me.getApplication().fireEvent('changecontentevent', view);

        me.refreshDeviceStatesGrid();
        me.refreshPrivilegesGrid();

        me.editProcessRecord = record;
    },

    saveProcess: function (button) {
        var me = this,
            editProcessPage = me.getEditProcessPage(),
            form = editProcessPage.down('#frm-edit-process'),
            editProcessRecord = editProcessPage.editProcessRecord,
            editProcessForm = editProcessPage.down('#frm-edit-process'),
            formErrorsPanel = editProcessForm.down('#form-errors');

        if (form.isValid()) {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            editProcessForm.updateRecord(editProcessRecord);

            editProcessRecord.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/managementprocesses').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editProcess.successMsg.saved', 'DBP', 'Process saved'));
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        editProcessForm.getForm().markInvalid(json.errors);
                    }
                    formErrorsPanel.show();
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },

    refreshDeviceStatesGrid: function () {
        var me = this,
            deviceStatesGrid = me.getDeviceStatesGrid(),
            editProcessForm = me.getEditProcessForm(),
            visibility = deviceStatesGrid.store.getCount() != 0;

        editProcessForm.down('#dbp-device-states-grid').setVisible(visibility);
        editProcessForm.down('#dbp-add-device-states-to-right-component').setVisible(visibility);
        editProcessForm.down('#empty-device-states-grid').setVisible(!visibility);
    },

    refreshPrivilegesGrid: function () {
        var me = this,
            privilegesGrid = me.getPrivilegesGrid(),
            editProcessForm = me.getEditProcessForm(),
            visibility = privilegesGrid.store.getCount() != 0;

        editProcessForm.down('#dbp-privileges-grid').setVisible(visibility);
        editProcessForm.down('#dbp-add-privileges-to-right-component').setVisible(visibility);
        editProcessForm.down('#empty-privileges-grid').setVisible(!visibility);
    },

    addDeviceStatesButton: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editProcessPage = me.getEditProcessPage(),
            editProcessForm = editProcessPage.down('#frm-edit-process'),
            deviceStatesGridStore = me.getDeviceStatesGrid().getStore(),
            deviceStates = [];

        editProcessForm.updateRecord(me.editProcessRecord);
        deviceStatesGridStore.each(function (record) {
            deviceStates.push(record.get('deviceStateId'));
        });

        if (router.arguments.activate) {
            router.getRoute('administration/managementprocesses/activate/deviceStates').forward({
                name: router.arguments.name,
                version: router.arguments.version
            }, {deviceState: deviceStates});
        }
        else {
            router.getRoute('administration/managementprocesses/edit/deviceStates').forward({
                name: router.arguments.name,
                version: router.arguments.version
            }, {deviceState: deviceStates});
        }
    },

    addDeviceStates: function (name, version) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allDeviceStatesForm, addDeviceStatesGrid,
            deviceStatesStore = me.getStore('Dbp.processes.store.DeviceStates');

        if (me.editProcessRecord == null) {
            me.forwardToPreviousPage();
            return;
        }
        allDeviceStatesView = Ext.create('Dbp.processes.view.AddDeviceStatesSetup', {editProcessRecord: me.getEditProcessPage().editProcessRecord});

        allDeviceStatesView.down('#btn-cancel-add-deviceStates').href = router.arguments.activate ?
            router.getRoute('administration/managementprocesses/activate').buildUrl({name: name, version: version}) :
            router.getRoute('administration/managementprocesses/edit').buildUrl({
                name: name,
                version: version
            });

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
            router = me.getController('Uni.controller.history.Router'),
            deviceStatesStore = me.getStore('Dbp.processes.store.DeviceStates'),
            addDeviceStatesGrid = me.getAddDeviceStatesGrid(),
            editProcessRecord = me.getAddDeviceStatePage().editProcessRecord;

        deviceStatesStore.each(function (record) {
            if (addDeviceStatesGrid.getSelectionModel().isSelected(record)) {
                var deviceState = Ext.create('Dbp.processes.model.DeviceState');
                deviceState.set('name', record.get('name'));
                deviceState.set('deviceState', record.get('deviceState'));
                deviceState.set('deviceStateId', record.get('deviceStateId'));
                deviceState.set('deviceLifeCycleId', record.get('deviceLifeCycleId'));
                editProcessRecord.deviceStates().add(deviceState);
            }
        });

        if (router.arguments.activate) {
            router.getRoute('administration/managementprocesses/activate').forward({
                name: router.arguments.name,
                version: router.arguments.version
            });
        }
        else {
            router.getRoute('administration/managementprocesses/edit').forward({
                name: router.arguments.name,
                version: router.arguments.version
            });
        }

    },

    addPrivilegesButton: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editProcessPage = me.getEditProcessPage(),
            editProcessForm = editProcessPage.down('#frm-edit-process'),
            privilegesGridStore = me.getPrivilegesGrid().getStore(),
            privileges = [];

        editProcessForm.updateRecord(me.editProcessRecord);
        privilegesGridStore.each(function (record) {
            privileges.push(record.get('name'));
        });

        if (router.arguments.activate) {
            router.getRoute('administration/managementprocesses/activate/privileges').forward({
                name: router.arguments.name,
                version: router.arguments.version
            }, {privilege: privileges});
        }
        else {
            router.getRoute('administration/managementprocesses/edit/privileges').forward({
                name: router.arguments.name,
                version: router.arguments.version
            }, {privilege: privileges});
        }
    },

    addPrivileges: function (name, version) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allPrivilegesForm, addPrivilegesGrid,
            privilegesStore = me.getStore('Dbp.processes.store.Privileges');

        if (me.editProcessRecord == null) {
            me.forwardToPreviousPage();
            return;
        }

        allPrivilegesView = Ext.create('Dbp.processes.view.AddPrivilegesSetup', {editProcessRecord: me.getEditProcessPage().editProcessRecord});
        allPrivilegesView.down('#btn-cancel-add-privileges').href = router.arguments.activate ?
            router.getRoute('administration/managementprocesses/activate').buildUrl({
                name: router.arguments.name,
                version: router.arguments.version
            }) :
            router.getRoute('administration/managementprocesses/edit').buildUrl({
                name: router.arguments.name,
                version: router.arguments.version
            });

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

                allPrivilegesView.setLoading(false);
            }
        });
    },

    addSelectedPrivileges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            privilegesStore = me.getStore('Dbp.processes.store.Privileges'),
            addPrivilegesGrid = me.getAddPrivilegesGrid(),
            editProcessRecord = me.getAddPrivilegesPage().editProcessRecord;

        if (me.editProcessRecord == null) {
            me.forwardToPreviousPage();
            return;
        }

        privilegesStore.each(function (record) {
            if (addPrivilegesGrid.getSelectionModel().isSelected(record)) {
                var privilege = Ext.create('Dbp.processes.model.Privilege');

                privilege.set('id', record.get('id'));
                privilege.set('applicationName', record.get('applicationName'));
                privilege.set('name', record.get('name'));
                privilege.set('userRoles', record.get('userRoles'));
                editProcessRecord.privileges().add(privilege);

            }
        });
        if (router.arguments.activate) {
            router.getRoute('administration/managementprocesses/activate').forward({
                name: router.arguments.name,
                version: router.arguments.version
            });
        }
        else {
            router.getRoute('administration/managementprocesses/edit').forward({
                name: router.arguments.name,
                version: router.arguments.version
            });
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
        router.arguments.name = record.get('name');
        router.arguments.version = record.get('version');

        switch (item.action) {
            case 'editProcess':
                me.editProcessRecord = null;
                route = 'administration/managementprocesses/edit';
                route && (route = router.getRoute(route));
                route && route.forward(router.arguments);
                break;
            case 'deactivateProcess':
                me.editProcessRecord = null;
                me.deactivateProcess(record);
                break;
            case 'activateProcess':
                me.editProcessRecord = null;
                route = 'administration/managementprocesses/activate';
                route && (route = router.getRoute(route));
                route && route.forward(router.arguments);
                break;
        }
    },

    removeDeviceState: function (record) {
        var me = this,
            editProcessPage = me.getEditProcessPage(),
            editProcessRecord = editProcessPage.editProcessRecord;

        editProcessRecord.deviceStates().remove(record);
        me.refreshDeviceStatesGrid();
    },

    removePrivileges: function (record) {
        var me = this,
            editProcessPage = me.getEditProcessPage(),
            editProcessRecord = editProcessPage.editProcessRecord;

        editProcessRecord.privileges().remove(record);
        me.refreshPrivilegesGrid();
    },

    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    }

});
