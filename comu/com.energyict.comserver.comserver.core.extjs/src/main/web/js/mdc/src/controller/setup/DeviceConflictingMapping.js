/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceConflictingMapping', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    mappingId: null,
    returnInfo: null,
    requires: [],

    views: [
        'Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingSetup',
        'Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingEdit',
        'Mdc.view.setup.deviceconflictingmappings.SingleSolution',
        'Mdc.view.setup.deviceconflictingmappings.MultiSolution'
    ],

    models: [
        'Mdc.model.ConflictingMapping'
    ],

    stores: [
        'DeviceConflictingMappings',
        'DeviceConflictingMappingsUnsolved'
    ],

    refs: [
        {ref: 'deviceConflictingActionMenu', selector: 'device-conflicting-mapping-action-menu'},
        {ref: 'conflictingMappingEditPanel', selector: '#conflictingMappingEditPanel'},
        {ref: 'connectionMethodsForm', selector: '#connectionMethodsForm'},
        {ref: 'securitySettingsForm', selector: '#securitySettingsForm'},
        {ref: 'afterSetsAdds', selector: '#afterSetsAdds'},
        {ref: 'afterConnectionsAdds', selector: '#afterConnectionsAdds'},
        {ref: 'connectionMethodsAddsPanel', selector: '#connectionMethodsAddsPanel'},
        {ref: 'securitySettingsAddsPanel', selector: '#securitySettingsAddsPanel'},
        {ref: 'deviceConflictingMappingEditPage', selector: 'deviceConflictingMappingEdit'}
    ],
    returnInfo : {from: 'unsolvedConflicts', id: ''},

    init: function () {
        this.control({
            '#device-conflicting-mapping-grid actioncolumn': {
                editConflictingMapping: this.redirectEdit,
                solveConflictingMapping: this.redirectSolve
            },
            '#conflictingMappingEditPanel #saveButton': {
                click: this.saveMapping
            }
        });
    },

    showOverview: function (deviceTypeId) {
        this.returnInfo = {from: 'unsolvedConflicts', id: deviceTypeId};
        this.showMappingsComplete('showUnsolved', deviceTypeId)
    },

    showAll: function (deviceTypeId) {
        this.returnInfo = {from: 'allConflicts', id: deviceTypeId};
        this.showMappingsComplete('showAll', deviceTypeId)
    },

    showMappingsComplete: function (configuration, deviceTypeId) {
        var me = this, mappingsStore, unsolved, widget,
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        switch (configuration) {
            case('showAll'):
            {
                mappingsStore = me.getStore('Mdc.store.DeviceConflictingMappings');
                unsolved = false;
            }
                break;
            case('showUnsolved'):
            {
                mappingsStore = me.getStore('Mdc.store.DeviceConflictingMappingsUnsolved');
                unsolved = true;
            }
                break;
        }
        mappingsStore.getProxy().setUrl(deviceTypeId);
        widget = Ext.widget('deviceConflictingMappingSetup', {
            deviceTypeId: deviceTypeId,
            store: mappingsStore,
            unsolved: unsolved
        });
        me.deviceTypeId = deviceTypeId;

        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.getApplication().fireEvent('changecontentevent', widget);
                if (unsolved) widget.down('uni-form-empty-message').setText(Uni.I18n.translate('deviceConflictingMappings.empty.unsolvedTitle', 'MDC', 'This device type has no unsolved conflicting device configuration mappings'));
                widget.down('deviceTypeSideMenu').setHeader(deviceType.get('name'));
                widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                viewport.setLoading(false);
            }
        });
    },

    showEdit: function (deviceTypeId, id) {
        var solve = false;
        this.showSolveEditView(deviceTypeId, id, solve);
    },

    showSolve: function (deviceTypeId, id) {
        var solve = true;
        this.showSolveEditView(deviceTypeId, id, solve);
    },

    redirectEdit: function (item) {
        var router = this.getController('Uni.controller.history.Router');
        location.href = router.getRoute('administration/devicetypes/view/conflictmappings/edit').buildUrl({
            deviceTypeId: this.deviceTypeId,
            id: encodeURIComponent(item.get('id'))
        });
    },

    redirectSolve: function (item) {
        var router = this.getController('Uni.controller.history.Router');
        location.href = router.getRoute('administration/devicetypes/view/conflictmappings/solve').buildUrl({
            deviceTypeId: this.deviceTypeId,
            id: encodeURIComponent(item.get('id'))
        });
    },

    showSolveEditView: function (deviceTypeId, id, solve) {
        var me = this, title, cancelLink,
            router = me.getController('Uni.controller.history.Router'),
            conflictingMappingModel = me.getModel('Mdc.model.ConflictingMapping'),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        me.deviceTypeId = deviceTypeId;

        switch (me.returnInfo.from) {
            case('unsolvedConflicts') :
            {
                cancelLink = router.getRoute('administration/devicetypes/view/conflictmappings').buildUrl({deviceTypeId: me.deviceTypeId});
            }
                break;
            case('allConflicts') :
            {
                cancelLink = router.getRoute('administration/devicetypes/view/conflictmappings/all').buildUrl({deviceTypeId: me.deviceTypeId});
            }
                break;
            case('changeDeviceConfiguration') :
            {
                cancelLink = router.getRoute('devices/device/changedeviceconfiguration').buildUrl({deviceId: me.returnInfo.id});
            }
                break;
            case('changeDeviceConfigurationBulk') :
            {
                cancelLink = router.getRoute('search/bulkAction').buildUrl();
            }
                break;
        }

        viewport.setLoading(true);
        conflictingMappingModel.getProxy().setUrl(deviceTypeId);
        me.mappingId = id;
        conflictingMappingModel.load(id, {
            success: function (record) {

                title = solve ? Uni.I18n.translate('deviceconfiguration.solveDeviceConflicting', 'MDC', 'Solve conflicting device configuration mapping')
                    : Uni.I18n.translate('deviceconfiguration.eidtDeviceConflicting', 'MDC', 'Edit conflicting device configuration mapping');

                var widget = Ext.widget('deviceConflictingMappingEdit', {
                    deviceTypeId: deviceTypeId,
                    fromConfig: record.get('fromConfiguration').name,
                    toConfig: record.get('toConfiguration').name,
                    cancelLink: cancelLink,
                    title: title,
                    conflictingMapping: record
                });
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {

                        Ext.suspendLayouts();

                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                        widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                        me.getApplication().fireEvent('changecontentevent', widget);

                        if (record.securitySets().getCount() > 0) {
                            me.addConnectionsAndSettings('securitySets', record);
                        } else {
                            me.getSecuritySettingsAddsPanel().setVisible(false);
                        }

                        if (record.connectionMethods().getCount() > 0) {
                            me.addConnectionsAndSettings('connectionMethods', record);
                        } else {
                            me.getConnectionMethodsAddsPanel().setVisible(false);
                        }

                        if (record.securitySetSolutions().getCount() > 0) {
                            me.applySolutions('SecuritySet', record);
                        }
                        if (record.connectionMethodSolutions().getCount() > 0) {
                            me.applySolutions('ConnectionMethod', record);
                        }

                        Ext.resumeLayouts(true);
                        viewport.setLoading(false);
                    }
                });
            }
        });
    },

    saveMapping: function () {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            page = me.getDeviceConflictingMappingEditPage(),
            cancelLink = page.cancelLink,
            record = page.conflictingMapping;

        viewport.setLoading(true);
        me.addSolutions(record);
        record.save({
            backUrl: cancelLink,
            success: function () {
                window.location.href = cancelLink;
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConflicting.acknowledgment.SolutionsAdded', 'MDC', 'Solutions added'));
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    addConnectionsAndSettings: function (conflicts, record) {
        var me = this, label, store, noAvailableLabel, actualAddPanel, actualForm, tooltipLabel;
        switch (conflicts) {
            case('connectionMethods'):
            {
                label = Uni.I18n.translate('deviceconfiguration.title.connectionMethods', 'MDC', 'Connection methods');
                tooltipLabel = Uni.I18n.translate('deviceconfiguration.tooltipLabel.connectionMethod', 'MDC', 'connection method');
                noAvailableLabel = Uni.I18n.translate('deviceconfiguration.lowercase.noConnectionMethodsAvailable', 'MDC', 'No connection methods available');

                store = record.connectionMethods();
                actualForm = me.getConnectionMethodsForm();
                actualAddPanel = me.getAfterConnectionsAdds();
            }
                break;
            case('securitySets'):
            {
                label = Uni.I18n.translate('deviceconfiguration.securitySettings', 'MDC', 'Security settings');
                tooltipLabel = Uni.I18n.translate('deviceconfiguration.tooltipLabel.securitySetting', 'MDC', 'security setting');
                noAvailableLabel = Uni.I18n.translate('deviceconfiguration.lowercase.noSecuritySettingAvailable', 'MDC', 'No security setting available');

                store = record.securitySets();
                actualForm = me.getSecuritySettingsForm();
                actualAddPanel = me.getAfterSetsAdds();
            }
                break;

        }
        actualForm.removeAll();
        if (store.getCount() > 0) {
            actualForm.add({
                xtype: 'displayfield',
                emptyValueDisplay: '',
                fieldLabel: label,
                labelAlign: 'left',
                labelWidth: '150px',
                style: {
                    margin: '0 20px',
                    padding: '0px'
                }
            });

            store.each(function (item) {

                if (item.to().getCount() == 1) {
                    actualForm.add({
                        xtype: 'single-solution-panel',
                        tooltipLabel: tooltipLabel,
                        conflict: item.get("from"),
                        toConfiguration: item.to().first(),
                        conflictsStore: store,
                        actualForm: actualForm,
                        actualAddPanel: actualAddPanel,
                        createAddLabel: me.createAddLabel,
                        conflicts: conflicts
                    });
                } else {
                    actualForm.add({
                        xtype: 'multi-solution-panel',
                        tooltipLabel: tooltipLabel,
                        conflict: item.get("from"),
                        toConfigurationStore: item.to(),
                        conflictsStore: store,
                        actualForm: actualForm,
                        actualAddPanel: actualAddPanel,
                        noAvailableLabel: noAvailableLabel,
                        createAddLabel: me.createAddLabel,
                        conflicts: conflicts
                    });
                }
                actualAddPanel.setValue(me.createAddLabel(store));
            });
        }


    },

    addSolutions: function (record) {
        var me = this, solution = {},
            connectionMethodValues = me.getConnectionMethodsForm().getValues(),
            securitySettingsValues = me.getSecuritySettingsForm().getValues(),
            securitySetSolutionsStore = record.securitySetSolutions(),
            connectionMethodSolutionsStore = record.connectionMethodSolutions(),
            conMethodsStore = record.connectionMethods(),
            securitySetsStore = record.securitySets();

        connectionMethodSolutionsStore.removeAll();
        conMethodsStore.each(function (item) {
            solution.from = item.get('from');
            if (connectionMethodValues[item.get('from').id] == 'remove') {
                solution.action = 'REMOVE';
                solution.to = {};
            } else {
                solution.action = "MAP";
                solution.to = item.to().getById(connectionMethodValues[item.get('from').id][1]).data;
            }
            connectionMethodSolutionsStore.add(solution);
        });

        securitySetSolutionsStore.removeAll();
        securitySetsStore.each(function (item) {
            solution.from = item.get('from');
            if (securitySettingsValues[item.get('from').id] == 'remove') {
                solution.action = 'REMOVE';
                solution.to = {};
            } else {
                solution.action = "MAP";
                solution.to = item.to().getById(securitySettingsValues[item.get('from').id][1]).data;
            }
            securitySetSolutionsStore.add(solution);
        });
    },

    applySolutions: function (typeOfSolutions, record) {
        var me = this, actualSolutions, actualForm, actualCombobox;

        switch (typeOfSolutions) {
            case 'SecuritySet' :
            {
                if (record.securitySetSolutions()) {
                    actualForm = me.getSecuritySettingsForm();
                    actualSolutions = record.securitySetSolutions();
                }
            }
                break;
            case 'ConnectionMethod' :
            {
                if (record.connectionMethodSolutions()) {
                    actualForm = me.getConnectionMethodsForm();
                    actualSolutions = record.connectionMethodSolutions();
                }
            }
                break;
        }

        actualSolutions.each(function (item) {
            if (item.get('action') == "MAP") {
                actualForm.down('#map' + item.get('from').id).setValue(true);
                actualCombobox = actualForm.down('#combo' + item.get('from').id);
                if (actualCombobox)
                    actualCombobox.setValue(actualCombobox.getStore().getById(item.get('to').id));
            }
        })

    },

    createAddLabel: function (store) {
        var addLabel = '', labels = [], rep = false;
        if (store.count() == 0) {
            addLabel = '';
        } else {
            store.each(function (items) {
                items.to().each(function (willAdded) {
                    for (var i = 0; i < labels.length; i++) {
                        if (labels[i] == willAdded.get('name')) rep = true;
                    }

                    if (!rep) labels.push(willAdded.get('name'));
                    rep = false;

                });
            });
            for (var i = 0; i < labels.length; i++) {
                if (labels.length == 1) {
                    addLabel = labels[i];
                } else if (i == labels.length - 1) {
                    addLabel += " and '" + labels[i];
                } else if (i == 0) {
                    addLabel += labels[i] + "' ";
                } else {
                    addLabel += ", '" + labels[i] + "' ";
                }
            }
        }
        return addLabel ? Ext.String.format(Uni.I18n.translate('deviceConflicting.willBeAdded', 'MDC', "'{0}' will be added."), addLabel) : addLabel;
    }
});