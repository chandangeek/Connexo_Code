/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.ComPortPools', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComPort',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    views: [
        'setup.comportpool.ComPortPoolsSetup',
        'setup.comport.OutboundComPortSelectionWindow'
    ],

    stores: [
        'ComPortPools',
        'ComPorts',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    refs: [
        {ref: 'comPortPoolsSetup', selector:'comPortPoolsSetup'},
        {ref: 'comPortPoolGrid', selector: '#comportpoolsgrid'},
        {ref: 'comPortPoolPreview', selector: '#comportpoolpreview'},
        {ref: 'previewActionMenu', selector: 'comPortPoolPreview #comportpoolViewMenu'}
    ],


    init: function () {
        this.timeUnitsStore = this.getStore('Mdc.store.TimeUnitsWithoutMilliseconds');
        this.timeUnitsStore.load();
        this.control({
            'comPortPoolsGrid': {
                select: this.showComPortPoolPreview
            },
            '#comportpoolViewMenu': {
                click: this.chooseAction,
                show: this.configureMenu
            },
            '#addComPortPoolMenu' : {
                click: this.addAction
            }
        });
    },

    configureMenu: function (menu) {
        var activate = menu.down('#activate'),
            deactivate = menu.down('#deactivate'),
            active = menu.record.data.active;

        if (active) {
            deactivate.show();
            activate.hide();
        } else {
            activate.show();
            deactivate.hide();
        }
    },

    addAction: function(menu, item){
        var router = this.getController('Uni.controller.history.Router');
        switch (item.action) {
            case 'addInbound':
                router.getRoute('administration/comportpools/addinbound').forward();
                break;
            case 'addOutbound':
                router.getRoute('administration/comportpools/addoutbound').forward();
                break;
        }
    },

    chooseAction : function (menu, item) {
        var me = this,
            gridView = me.getComPortPoolGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            form = me.getComPortPoolPreview().down('form'),
            activeChange = 'notChange';

        switch (item.action) {
            case 'edit':
                me.editComPortPool(record);
                break;
            case 'remove':
                me.showDeleteConfirmation(record);
                break;
            case 'activate':
                activeChange = true;
                break;
            case 'deactivate':
                activeChange = false;
                break;
        }

        if (activeChange != 'notChange') {
            record.set('active', activeChange);
            record.save({
                isNotEdit: true,
                success: function (model) {
                    var msg = activeChange ? Uni.I18n.translate('general.activated', 'MDC', 'activated') :
                        Uni.I18n.translate('general.deactivated', 'MDC', 'deactivated');
                    gridView.refresh();
                    form.loadRecord(model);
                    me.getPreviewActionMenu().record = model;
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.comPortPoolMsg', 'MDC', 'Communication port pool {0}',[msg]));
                },
                failure: function () {
                    record.reject();
                }
            });
        }

    },

    modelToForm: function (model, form) {
        var me = this,
            basicForm = form.getForm(),
            values = {},
            taskExecutionTimeoutUnit = me.timeUnitsStore.findRecord('timeUnit', model.get('taskExecutionTimeout').timeUnit);
        if (taskExecutionTimeoutUnit) {
            taskExecutionTimeout = {count: model.get('taskExecutionTimeout').count, timeUnit: taskExecutionTimeoutUnit.get('localizedValue')},
            model.beginEdit();
            model.set('taskExecutionTimeout', taskExecutionTimeout);
            model.endEdit();
        }
        Ext.Object.each(model.getData(), function (key, value) {
                values[key] = value;
        });
        basicForm.setValues(values);
    },

    showComPortPoolPreview: function (selectionModel, record) {
        var me = this,
            itemPanel = this.getComPortPoolPreview(),
            form = itemPanel.down('form'),
            model = this.getModel('Mdc.model.ComPortPool'),
            id = record.getId(),
            pctHighPrioTasks = form.down('[name=pctHighPrioTasks]'),
            maxPriorityConnections =  form.down('[name=maxPriorityConnections]'),
            deviceDiscoveryProtocolsStore = this.getStore('Mdc.store.DeviceDiscoveryProtocols'),
            taskExecutionTimeout = form.down('[name=taskExecutionTimeout]');
        itemPanel.setLoading(this.getModel('Mdc.model.ComPortPool'));
        model.load(id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    if(record.get('direction').toLowerCase() == 'outbound') {
                        form.down('#discoveryProtocolPluggableClassId').hide();

                        pctHighPrioTasks.show();
                        maxPriorityConnections.show();
                        taskExecutionTimeout.show();
                    }
                    else {
                        form.down('#discoveryProtocolPluggableClassId').show();
                        pctHighPrioTasks.hide();
                        maxPriorityConnections.hide();
                        taskExecutionTimeout.hide();
                    }
                    form.loadRecord(record);
                    me.modelToForm(record, form);

                    if (!deviceDiscoveryProtocolsStore.getCount()){
                        deviceDiscoveryProtocolsStore.load(function (records) {
                            var deviceDiscoveryProtocols = records.filter(function (deviceDiscoveryProtocol) {
                                return deviceDiscoveryProtocol.get('id') == record.get('discoveryProtocolPluggableClassId');
                            });

                            deviceDiscoveryProtocols.length > 0 && form.down('#discoveryProtocolPluggableClassId').setValue(deviceDiscoveryProtocols[0].get('name'));
                        });
                    }
                    else {
                        var deviceDiscoveryProtocol = deviceDiscoveryProtocolsStore.getById(record.get('discoveryProtocolPluggableClassId'));
                        form.down('#discoveryProtocolPluggableClassId').isVisible() && form.down('#discoveryProtocolPluggableClassId').setValue(deviceDiscoveryProtocol.get('name'));

                    }

                    var menuItem = form.up('panel').down('menu');
                    if(menuItem)
                        menuItem.record = record;
                    itemPanel.setLoading(false);
                    itemPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
                }
            }
        });
    },

    editComPortPool: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/comportpools/detail/edit').forward({id: id});
    },

    showDeleteConfirmation: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comPortPool.deleteConfirmation.msg', 'MDC', 'This communication port pool will no longer be available.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.deleteComPortPool(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteComPortPool: function (record) {
        var me = this,
            page = me.getComPortPoolsSetup(),
            gridToolbarTop = me.getComPortPoolGrid().down('pagingtoolbartop');
        page.setLoading('Removing...');
        record.destroy({
            callback: function (model, operation) {
                var message ='', errorCode = '';
                page.setLoading(false);
                if (operation.response.status == 204) {
                    gridToolbarTop.totalCount = 0;
                    me.getComPortPoolGrid().getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortPool.deleteSuccess.msg', 'MDC', 'Communication port pool removed'));
                } else if (operation.response.status == 400) {
                    if (!Ext.isEmpty(response.responseText)) {
                        var json = Ext.decode(response.responseText, true);
                        if (json && json.error) {
                            message = json.error;
                        }
                        if (json && json.errorCode) {
                            errorCode = json.errorCode;
                        }
                    }

                    me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('general.during.removing', 'MDC', 'Error during removing'), 'Error during removing.'+response.responseText, errorCode);
                }
            }
        });
    }
});
