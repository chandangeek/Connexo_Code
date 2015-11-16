Ext.define('Dbp.processes.controller.Processes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Dbp.privileges.DeviceProcesses',
        'Dbp.processes.store.Processes'
    ],
    views: [
        'Dbp.processes.view.Processes'
    ],
    models: [
        'Dbp.processes.model.Process'
    ],
    stores: [
        'Dbp.processes.store.Processes'
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
        }
    ],

    init: function () {
        this.control({
            'dbp-processes dbp-processes-grid': {
                select: this.showPreview
            },
            'dbp-process-action-menu': {
                show: this.onMenuShow,
                click: this.chooseAction
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

                if (operation.getResultSet().records){

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
        record.set('active', record.get('active') === 'ACTIVE'? 'INACTIVE': 'ACTIVE');
        record.endEdit(true);

        processesForm.setLoading();

        record.save({
            success: function (rec, operation) {

                if (operation.getResultSet().records){
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

    editProcess: function (processId) {
        alert(processId);
    },

    onMenuShow: function (menu) {
        if (menu.record.get('active') === 'ACTIVE') {
            menu.down('#menu-activate-process').hide();
            menu.down('#menu-deactivate-process').show();
        } else {
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
                route = 'administration/managementprocesses/editProcess';
                route && (route = router.getRoute(route));
                route && route.forward(router.arguments);
                break;
            case 'deactivateProcess':
                me.deactivateProcess(record);
                break;
            case 'activateProcess':
                me.changeProcessActivation(record);
                return;
        }
    }
});