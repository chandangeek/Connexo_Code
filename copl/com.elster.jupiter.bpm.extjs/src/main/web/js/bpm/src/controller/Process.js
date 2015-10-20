Ext.define('Bpm.controller.Process', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.privileges.BpmManagement',
        'Bpm.store.process.Processes',
        'Bpm.view.process.AddProcessesGrid'
    ],
    views: [
        'Bpm.view.process.Processes',
        'Bpm.view.process.AddProcessesSetup'
    ],
    models: [
        'Bpm.model.process.Process',
        'Bpm.model.process.BpmProcesses'
    ],
    stores: [
        'Bpm.store.process.Processes',
        'Bpm.store.process.BpmProcesses'
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
            ref: 'processesForm',
            selector: 'bpm-processes #bpm-processes-form'
        },
        {
            ref: 'addProcessesGrid',
            selector: 'usr-add-processes-setup #grd-add-processes'
        }
    ],

    init: function () {
        this.control({
            'bpm-processes bpm-processes-grid': {
                select: this.showPreview
            },
            'bpm-process-action-menu': {
                show: this.onMenuShow,
                click: this.chooseAction
            },
            '#btn-add-processes': {
                click: this.saveAddedProcesses
            }
        });
    },

    showProcesses: function () {
        var me = this,
            view;

        view = Ext.widget('bpm-processes', {});
        me.getApplication().fireEvent('onBeforeLoad', view);
        me.getApplication().fireEvent('changecontentevent', view);
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

    changeProcessActivation: function (record) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            processesForm = me.getProcessesForm(),
            isActive = record.get('active');

        record.beginEdit();
        record.set('active', !isActive);
        record.endEdit(true);

        processesForm.setLoading();

        record.save({
            success: function (record, operation) {

                processesForm.down('bpm-process-preview').loadRecord(record);
                if (isActive) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.process.deactivate', 'BPM', 'Process deactivated'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.process.activate', 'BPM', 'Process activated'));
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

    addProcesses: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            bpmProcessesStore = me.getStore('Bpm.store.process.BpmProcesses'),
            processesStore = me.getStore('Bpm.store.process.Processes'),
            addProcessesView = Ext.create('Bpm.view.process.AddProcessesSetup');

        bpmProcessesStore.loadData([], false);
        me.getApplication().fireEvent('changecontentevent', addProcessesView);

        bpmProcessesStore.load({
            callback: function (records, operation, success) {

                processesStore.load({
                    callback: function (records, operation, success) {
                        records.forEach(function (record) {
                            var rowIndex = bpmProcessesStore.findExact('name', record.get('name'));

                            // remove
                            if (rowIndex < 2) {
                                return;
                            }
                            if (rowIndex != -1) {
                                bpmProcessesStore.removeAt(rowIndex);
                            }
                        });
                        addProcessesView.setLoading(false);
                    }
                });
            }
        });
    },

    saveAddedProcesses: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            bpmProcessesStore = me.getStore('Bpm.store.process.BpmProcesses'),
            processList = [];

        var processes = Ext.create(Bpm.model.process.BpmProcesses);
        var addProcessesGrid = me.getAddProcessesGrid();

        bpmProcessesStore.each(function (record) {
            if (addProcessesGrid.getSelectionModel().isSelected(record)) {

                var user = Ext.create(Bpm.model.process.Process);
                user.set('name', record.get('name'));
                processList.push(user);
            }
        });
        processes.processes().add(processList);
        processes.save({
            success: function (record) {
                router.getRoute('administration/managementprocesses').forward();
            },
            failure: function (record, operation) {

            }
        });
    },

    onMenuShow: function (menu) {
        if (menu.record.get('active')) {
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
            case 'activateProcess':
            case 'deactivateProcess':
                me.changeProcessActivation(record);
                return;
        }
    }
});