/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.controller.Processes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.processes.store.Processes',
        'Uni.controller.history.Router',
        'Uni.view.container.ContentContainer'
    ],
    views: [
        'Bpm.processes.view.Processes',
        'Bpm.processes.view.EditProcess'
    ],
    models: [
        'Bpm.processes.model.Process',
        'Bpm.processes.model.EditProcess',
        'Bpm.processes.model.Privilege'
    ],
    stores: [
        'Bpm.processes.store.Processes',
        'Bpm.processes.store.Associations',
        'Bpm.processes.store.Privileges'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'bpm-processes'
        },
        {
            ref: 'mainGrid',
            selector: 'bpm-processes bpm-processes-grid'
        },
        {
            ref: 'btnPreviewAction',
            selector: 'bpm-processes #btn-preview-action'
        },
        {
            ref: 'processesForm',
            selector: 'bpm-processes #bpm-processes-form'
        },
        {
            ref: 'editProcessPage',
            selector: 'bpm-edit-process'
        },
        {
            ref: 'editProcessForm',
            selector: '#frm-edit-process'
        },
        {
            ref: 'associatedCombo',
            selector: 'bpm-edit-process #cbo-associated-to'
        },
        {
            ref: 'propertyForm',
            selector: 'bpm-edit-process property-form'
        },
        {
            ref: 'customForm',
            selector: 'bpm-edit-process #custom-form'
        },
        {
            ref: 'processForm',
            selector: 'bpm-edit-process #processes-form'
        },
        {
            ref: 'privilegesGrid',
            selector: 'bpm-edit-process #bpm-privileges-grid'
        }
    ],

    init: function () {
        this.control({
            'bpm-processes bpm-processes-grid': {
                select: this.showPreview
            },
            'bpm-process-action-menu': {
                click: this.chooseAction
            },
            'bpm-edit-process #cbo-associated-to': {
                select: this.updateAssociatedData
            },

            '#frm-edit-process #btn-save': {
                click: this.saveProcess
            }
        });
    },

    showProcesses: function () {
        var me = this,
            view;

        Ext.create('Bpm.processes.store.Associations').load({

            callback: function (records, operation, success) {
                view = Ext.widget('bpm-processes', {disableAction: !(success && (records.length > 0))});

                me.getApplication().fireEvent('onBeforeLoad', view);
                me.getApplication().fireEvent('changecontentevent', view);
                me.getBtnPreviewAction().setVisible((success && (records.length > 0)));
            }
        });


    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('bpm-process-preview'),
            previewForm = page.down('bpm-process-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('bpm-process-action-menu') && (preview.down('bpm-process-action-menu').record = record);
        Ext.resumeLayouts();
    },

    deactivateProcess: function (record) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            processesForm = me.getProcessesForm();

        record.beginEdit();
        record.set('active', 'INACTIVE');
        record.endEdit(true);
        record.getProxy().setUrl('/api/bpm/runtime/process/deactivate');
        processesForm.setLoading();
        record.save({
            success: function (rec, operation) {

                if (operation.getResultSet().records) {

                    var retRecord = operation.getResultSet().records[0];
                    retRecord.set('deploymentId', rec.get('deploymentId'));
                    processesForm.down('#frm-preview-process').loadRecord(retRecord);
                    if (retRecord.get('active') === 'INACTIVE') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('Bpm.process.deactivate', 'BPM', 'Process deactivated'));
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
            editProcessModel = me.getModel('Bpm.processes.model.EditProcess'),
            view = Ext.widget('bpm-edit-process'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        // load associations store
        viewport.setLoading();
        me.getAssociatedCombo().getStore().load({
            callback: function (records, operation, success) {

                // load privileges store
                me.getStore('Bpm.processes.store.Privileges').load({
                    callback: function (records, operation, success) {

                        // load process
                        if (activate) {
                            editProcessModel.getProxy().extraParams = {
                                version: version,
                                association: me.getAssociatedCombo().getStore().first().get('type')
                            };
                        }
                        else {
                            editProcessModel.getProxy().extraParams = {version: version};
                        }
                        editProcessModel.getProxy().setUrl('/api/bpm/runtime/process');
                        editProcessModel.load(name, {
                            success: function (process) {
                                me.loadEditProcess(name, version, activate, process, view);
                            },
                            failure: function (record, operation) {
                                view.destroy();
                            }
                        });
                    }
                });
            }
        });
    },

    activateProcess: function (name, version, activate) {
        var me = this,
            editProcessModel = me.getModel('Bpm.processes.model.EditProcess'),
            view = Ext.widget('bpm-edit-process'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        // load associations store
        viewport.setLoading();
        me.getAssociatedCombo().getStore().load({
            callback: function (records, operation, success) {

                // load privileges store
                me.getStore('Bpm.processes.store.Privileges').load({
                    callback: function (records, operation, success) {

                        editProcessModel.getProxy().extraParams = {
                            version: version,
                            association: me.getAssociatedCombo().getStore().first().get('type')
                        };

                        editProcessModel.getProxy().setUrl('/api/bpm/runtime/process');
                        editProcessModel.load(name, {
                            success: function (process) {
                                if (process.get('type') != '') {
                                    editProcessModel.getProxy().extraParams = {
                                        version: version,
                                        association: process.get('type')
                                    };
                                    editProcessModel.load(name, {
                                        success: function (process) {
                                            me.getAssociatedCombo().setValue(process.get('type'));
                                            me.loadEditProcess(name, version, activate, process, view);
                                        }
                                    });
                                }
                                else {
                                    me.loadEditProcess(name, version, activate, process, view);
                                }
                            },
                            failure: function (record, operation) {
                                view.destroy();
                            }
                        });
                    }
                });
            }
        });
    },

    loadEditProcess: function (name, version, activate, record, view) {
        var me = this,
            editProcessForm = view.down('#frm-edit-process'),
            privilegesGrid, propertyForm,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.getApplication().fireEvent('changecontentevent', view);

        editProcessForm.editProcessRecord = record;
        me.getApplication().fireEvent(activate ? 'activateProcesses' : 'editProcesses', name + ':' + version);

        if (record.get('type') == '') {
            me.getAssociatedCombo().setValue(me.getAssociatedCombo().getStore().first().get(me.getAssociatedCombo().valueField));
        }
        else {
            editProcessForm.loadRecord(record);
        }

        privilegesGrid = me.getPrivilegesGrid();
        propertyForm = me.getPropertyForm();

        // check selected privileges
        privilegesGrid.getSelectionModel().suspendChanges();
        privilegesGrid.getSelectionModel().select(record.privileges().data.items, false, true);
        privilegesGrid.getSelectionModel().resumeChanges();
        privilegesGrid.fireEvent('selectionchange');

        // load property form
        if (record && record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
        propertyForm.up('#frm-edit-process').doLayout();
        //  me.getApplication().fireEvent('changecontentevent', view);
        if (activate) {
            record.set('name', name);
            record.set('version', version);
            record.set('active', 'ACTIVE');
            editProcessForm.setTitle(Uni.I18n.translate('editProcess.activate', 'BPM', "Activate '{0}'", name + ':' + version));
        }
        else {
            editProcessForm.setTitle(Uni.I18n.translate('editProcess.edit', 'BPM', "Edit '{0}'", name + ':' + version));
        }
        viewport.setLoading(false);
    },

    updateAssociatedData: function (control, records) {
        var me = this,
            record = records[0] ? records[0] : records,
            editProcessModel = me.getModel('Bpm.processes.model.EditProcess'),
            router = me.getController('Uni.controller.history.Router'),
            version = router.arguments.version,
            name = router.arguments.name,
            editProcessForm = me.getEditProcessPage().down('#frm-edit-process'),
            privilegesGrid = me.getPrivilegesGrid(),
            propertyForm = me.getPropertyForm();

        // load process
        editProcessModel.getProxy().setUrl('/api/bpm/runtime/process');
        editProcessModel.getProxy().extraParams = {version: version, association: record.get('type')};
        editProcessModel.load(name, {
            success: function (process) {
                process.set('nanme', editProcessForm.editProcessRecord.get('name'));
                process.set('version', editProcessForm.editProcessRecord.get('version'));
                process.set('active', editProcessForm.editProcessRecord.get('active'));
                editProcessForm.editProcessRecord = process;

                // load property form
                if (process && process.properties() && process.properties().count()) {
                    propertyForm.loadRecord(process);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
                propertyForm.up('#frm-edit-process').doLayout();
            }
        });
    },

    saveProcess: function (button) {
        var me = this,
            editProcessPage = me.getEditProcessPage(),
            propertyForm = me.getPropertyForm(),
            privilegesGrid = me.getPrivilegesGrid(),
            editProcessForm = editProcessPage.down('#frm-edit-process'),
            formErrorsPanel = editProcessForm.down('#form-errors'),
            editProcessRecord = editProcessForm.editProcessRecord;


        editProcessForm.getForm().clearInvalid();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        editProcessForm.updateRecord(editProcessRecord);
        editProcessRecord.beginEdit();
        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            editProcessRecord.propertiesStore = propertyForm.getRecord().properties();
        }

        editProcessRecord.privileges().removeAll();
        var selectedPrivileges = privilegesGrid.getSelectionModel().getSelection();
        Ext.Array.each(selectedPrivileges, function (record) {
            var privilege = Ext.create('Bpm.processes.model.Privilege');

            privilege.set('id', record.get('id'));
            privilege.set('applicationName', record.get('applicationName'));
            privilege.set('name', record.get('name'));
            privilege.set('userRoles', record.get('userRoles'));
            editProcessRecord.privileges().add(privilege);
        });

        editProcessRecord.endEdit();
        editProcessRecord.getProxy().setUrl('/api/bpm/runtime/process/activate');
        editProcessRecord.save({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute('administration/managementprocesses').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editProcess.successMsg.saved', 'BPM', 'Process saved'));
            },
            failure: function (record, operation) {
                if (operation.response.status === 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        editProcessForm.getForm().markInvalid(json.errors);
                    }
                    formErrorsPanel.show();
                }
            }
        })
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
    }
});
