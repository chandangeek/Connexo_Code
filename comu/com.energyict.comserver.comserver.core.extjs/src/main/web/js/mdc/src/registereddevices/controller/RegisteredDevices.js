/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.controller.RegisteredDevices', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.registereddevices.view.RegisteredDevices',
        'Mdc.registereddevices.view.RegisteredDevicesOnGateway',
        'Mdc.registereddevices.view.RegisteredDevicesKPIs',
        'Mdc.registereddevices.view.AddEditView',
        'Mdc.registereddevices.view.ActionMenu'
    ],

    models: [
        'Mdc.registereddevices.model.RegisteredDevicesKPI',
        'Mdc.registereddevices.model.RegisteredDevicesOnGateway',
        'Mdc.registereddevices.model.AvailableDeviceGroup',
        'Mdc.registereddevices.model.AvailableKPI',
        'Mdc.registereddevices.model.RegisteredDevicesKPIsData',
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies',
        'Mdc.registereddevices.store.RegisteredDevicesKPIs',
        'Mdc.registereddevices.store.AvailableDeviceGroups',
        'Mdc.registereddevices.store.AvailableKPIs',
        'Mdc.registereddevices.store.RegisteredDevicesKPIsData',
        'Mdc.registereddevices.store.RegisteredDevicesOnGateway'
    ],

    refs: [
        {ref: 'kpiEditForm', selector: '#mdc-registered-devices-kpi-addedit-form'},
        {ref: 'kpiPreviewForm', selector: '#mdc-registered-devices-kpi-details-form'},
        {ref: 'kpiPreviewContainer', selector: '#mdc-registered-devices-kpi-preview'},
        {ref: 'kpisGrid', selector: 'registered-devices-kpis-grid'},
        {ref: 'kpisOverview', selector: 'registered-devices-kpis-view'},
        {ref: 'registeredDevicesView', selector: 'registered-devices-view'},
        {ref: 'registeredDevicesOnGatewayView', selector: 'registered-devices-on-gateway-view'}
    ],

    init: function () {
        this.control({
            'registered-devices-kpis-view #mdc-registered-devices-kpis-grid': {
                 select: this.showKpiPreview
            },
            'registered-devices-kpis-grid #mdc-grid-registered-devices-kpi-add': {
                click: this.moveToAddPage
            },
            'registered-devices-kpis-view #mdc-no-registered-devices-kpis-add': {
                click: this.moveToAddPage
            },
            'registered-devices-view #mdc-registered-devices-view-add-kpi' : {
                click: this.moveToAddPage
            },
            'registered-devices-kpi-action-menu': {
                click: this.chooseAction
            },
            'registered-devices-kpi-addedit #mdc-registered-devices-kpi-add-cancelLink': {
                 click: this.moveToViewPage
            },
            'registered-devices-kpi-addedit #mdc-registered-devices-kpi-add-addEditButton': {
                click: this.onAddClicked
            },
            'registered-devices-view': {
                beforedestroy: this.onDestroyRegisteredDevicesView
            }
        });
    },

    showRegisteredDevices: function() {
        var me = this,
            widget = Ext.widget('registered-devices-view'),
            kpiStore = widget.down('#mdc-registered-devices-device-group-filter').getStore(),
            kpiDataStore = Ext.getStore('Mdc.registereddevices.store.RegisteredDevicesKPIsData');

        kpiStore.load(function(kpiRecords) {
            if (kpiRecords.length === 0) {
                widget.down('#mdc-registered-devices-view-no-kpis').show();
                widget.down('#mdc-registered-devices-view-no-data').hide();
                widget.down('#mdc-registered-devices-filters').hide();
                widget.down('#mdc-registered-devices-graph').hide();
            } else {
                if (Ext.isEmpty(widget.down('#mdc-registered-devices-device-group-filter').getValue())) {
                    widget.down('#mdc-registered-devices-device-group-filter').setValue(kpiRecords[0].get('id'));
                }
                widget.down('#mdc-registered-devices-view-no-kpis').hide();
                widget.down('#mdc-registered-devices-view-no-data').hide();
                widget.down('#mdc-registered-devices-filters').show();
                widget.down('#mdc-registered-devices-graph').show();
            }
            me.getApplication().fireEvent('changecontentevent', widget);
            kpiDataStore.on('load', me.onLoadKPIDataStore, me);
        });
    },

    onLoadKPIDataStore: function(store, records) {
        var me = this,
            registeredDevicesView = me.getRegisteredDevicesView(),
            deviceGroupCombo = registeredDevicesView.down('#mdc-registered-devices-device-group-filter'),
            kpiStore = registeredDevicesView.down('#mdc-registered-devices-device-group-filter').getStore(),
            indexInStore = kpiStore.findExact('id', deviceGroupCombo.getValue()),
            storeRecord = indexInStore === -1 ? null : kpiStore.getAt(indexInStore),
            graph = registeredDevicesView.down('#mdc-registered-devices-graph');

        if (records.length === 0) {
            registeredDevicesView.down('#mdc-registered-devices-view-no-data').show();
            graph.hide();
        } else if (storeRecord && graph) {
            registeredDevicesView.down('#mdc-registered-devices-view-no-data').hide();
            graph.show();
            graph.data = records;
            graph.period = me.getRegisteredDevicesView().down('#mdc-registered-devices-period-filter').getParamValue();
            graph.frequency = me.determineFrequency(storeRecord);
            graph.drawGraph();
        }
    },

    determineFrequency: function(record) {
        var freq = record.get('frequency') ? record.get('frequency').every : undefined;
        if (freq) {
            if (freq.timeUnit === 'minutes') {
                return '15m'
            } else if (freq.timeUnit === 'hours') {
                if (freq.count === 4) {
                    return '4h';
                } else if (freq.count === 12) {
                    return '12h';
                }
            } else if (freq.timeUnit === 'days') {
                return '1d'
            }
        }
        return undefined;
    },

    showRegisteredDevicesKpis: function() {
        var me = this,
            widget = Ext.widget('registered-devices-kpis-view');

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    moveToViewPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/regdeviceskpis').forward();
    },

    moveToAddPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/regdeviceskpis/add').forward();
    },

    showKpiPreview: function (selectionModel, record) {
        var preview = this.getKpiPreviewContainer();

        Ext.suspendLayouts();
        if (preview.down('registered-devices-kpi-action-menu')) {
            preview.down('registered-devices-kpi-action-menu').record = record;
        }
        preview.setTitle(Ext.String.htmlEncode(record.get('deviceGroup').name));
        this.getKpiPreviewForm().loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showEditKPIView: function(id) {
        var me = this,
            widget = Ext.widget('registered-devices-kpi-addedit'),
            deviceGroupStore = widget.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.registereddevices.model.RegisteredDevicesKPI'),
            form = widget.down('#mdc-registered-devices-kpi-addedit-form'),
            deviceGroupCombo = widget.down('#mdc-registered-devices-kpi-add-device-group-combo'),
            deviceGroupDisplayField = widget.down('#mdc-registered-devices-kpi-add-device-group-displayField'),
            createBtn = widget.down('#mdc-registered-devices-kpi-add-addEditButton');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (!Ext.isEmpty(id)) {
                    form.setTitle(Uni.I18n.translate('registeredDevicesKPIs.edit', 'MDC', 'Edit registered devices KPI'));
                    kpiModel.load(id, {
                        success: function (kpiRecord) {
                            Ext.suspendLayouts();
                            form.loadRecord(kpiRecord);
                            form.down('[name=deviceGroup]').disable();
                            form.down('[name=frequency]').disable();
                            form.setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", kpiRecord.get('deviceGroup').name));
                            me.getApplication().fireEvent('loadRegisteredDevicesKpi', kpiRecord.get('deviceGroup').name);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';
                            Ext.resumeLayouts(true);
                            widget.setLoading(false);
                        }
                    });
                } else {
                    if (deviceGroupStore.getCount() > 0) {
                        form.loadRecord(Ext.create(kpiModel));
                        if (deviceGroupStore.getCount() === 1) {
                            deviceGroupCombo.setValue(deviceGroupStore.getAt(0).get('id'));
                        }
                    } else {
                        Ext.suspendLayouts();
                        deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('general.noDeviceGroup', 'MDC', 'No device group defined yet.') +  '</span>' );
                        deviceGroupDisplayField.show();
                        deviceGroupCombo.hide();
                        createBtn.disable();
                        Ext.resumeLayouts(true);
                    }
                    form.setTitle(Uni.I18n.translate('registeredDevicesKPIs.add', 'MDC', 'Add registered devices KPI'));
                    widget.setLoading(false);
                }
            }
        });
    },

    onAddClicked: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getKpiEditForm(),
            record = editForm.getRecord(),
            errorMsgPnl = editForm.down('uni-form-error-message'),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            target = editForm.down('[name=target]').getValue(),
            backUrl = router.getRoute('administration/regdeviceskpis').buildUrl();

        errorMsgPnl.hide();
        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id,
                name: record.get('deviceGroup').name
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies').getById(frequency).get('value'));
        }
        record.set('target', target);
        record.endEdit();
        record.save({
            backUrl: backUrl,
            success: function (record, operation) {
                var successMessage = '';
                switch (operation.action) {
                    case 'update':
                        successMessage = Uni.I18n.translate('registeredDevicesKPIs.saved', 'MDC', 'Registered devices KPI saved');
                        break;
                    case 'create':
                        successMessage = Uni.I18n.translate('registeredDevicesKPIs.added', 'MDC', 'Registered devices KPI added');
                        break;
                }
                window.location.href = backUrl;
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    errorMsgPnl.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function() {
                editForm.setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'edit':
                router.getRoute('administration/regdeviceskpis/edit').forward({id: menu.record.get('id')});
                break;
            case 'remove':
                Ext.create('Uni.view.window.Confirmation').show({
                    title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", menu.record.get('deviceGroup').name),
                    msg: Uni.I18n.translate('registeredDevicesKPIs.deleteConfirmation.msg', 'MDC', 'This registered devices KPI will no longer be available.'),
                    fn: function (state) {
                        switch (state) {
                            case 'confirm':
                                me.removeKpi(menu.record);
                                break;
                        }
                    }
                });
        }
    },

    removeKpi: function (record) {
        var me = this,
            overview = me.getKpisOverview(),
            grid = me.getKpisGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop');

        overview.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            success: function () {
                gridToolbarTop.isFullTotalCount = false;
                gridToolbarTop.totalCount = -1;
                grid.down('pagingtoolbarbottom').totalCount--;
                grid.getStore().loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registeredDevicesKPIs.removed', 'MDC', 'Registered devices KPI removed'));
            },
            callback: function () {
                overview.setLoading(false);
            }
        });
    },

    onDestroyRegisteredDevicesView: function() {
        kpiDataStore = Ext.getStore('Mdc.registereddevices.store.RegisteredDevicesKPIsData');
        kpiDataStore.un('load', this.onDestroyRegisteredDevicesView);
    },

    showRegisteredDevicesOnGateway: function(deviceName) {
        var me = this,
            widget = undefined,
            kpiDataStore = Ext.getStore('Mdc.registereddevices.store.RegisteredDevicesOnGateway');

        kpiDataStore.getProxy().setUrl(deviceName);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
            success: function (record) {
                widget = Ext.widget('registered-devices-on-gateway-view', {device: record});
                if (Ext.isEmpty(widget.down('#mdc-registered-devices-on-gateway-frequency-combo').getValue())) {
                    widget.down('#mdc-registered-devices-on-gateway-frequency-combo').setValue('1days');
                }
                me.getApplication().fireEvent('loadDevice', record);
                me.getApplication().fireEvent('changecontentevent', widget);
                kpiDataStore.on('load', me.onLoadDataStore, me);
            }
        });
    },

    onLoadDataStore: function(store, records) {
        var me = this,
            registeredDevicesView = me.getRegisteredDevicesOnGatewayView(),
            frequencyCombo = registeredDevicesView.down('#mdc-registered-devices-on-gateway-frequency-combo'),
            frequencyStore = frequencyCombo.getStore(),
            indexInStore = frequencyStore.findExact('id', frequencyCombo.getValue()),
            frequencyRecord = indexInStore === -1 ? null : frequencyStore.getAt(indexInStore),
            graph = registeredDevicesView.down('#mdc-registered-devices-graph');

        if (frequencyRecord && graph) {
            registeredDevicesView.down('#mdc-registered-devices-graph').show();
            graph.showTargetAndTotal = false;
            graph.data = records;
            graph.period = me.getRegisteredDevicesOnGatewayView().down('#mdc-registered-devices-on-gateway-period-filter').getParamValue();
            switch(frequencyRecord.get('id')) {
                case '15minutes':
                    graph.frequency = '15m';
                    break;
                case '4hours':
                    graph.frequency = '4h';
                    break;
                case '12hours':
                    graph.frequency = '12h';
                    break;
                case '1days':
                default:
                    graph.frequency = '1d';
            }
            graph.drawGraph();
        }
    }
});

