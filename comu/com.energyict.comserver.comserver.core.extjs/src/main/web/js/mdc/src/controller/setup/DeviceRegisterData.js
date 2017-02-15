/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceRegisterData', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterdata.MainSetup',
        'setup.deviceregisterdata.MainGrid',
        'setup.deviceregisterdata.text.Setup',
        'setup.deviceregisterdata.text.Grid',
        'setup.deviceregisterdata.text.Preview',
        'setup.deviceregisterdata.numerical.Setup',
        'setup.deviceregisterdata.numerical.Grid',
        'setup.deviceregisterdata.numerical.Preview',
        'setup.deviceregisterdata.billing.Setup',
        'setup.deviceregisterdata.billing.Grid',
        'setup.deviceregisterdata.billing.Preview',
        'setup.deviceregisterdata.flags.Setup',
        'setup.deviceregisterdata.flags.Grid',
        'setup.deviceregisterdata.flags.Preview',
        'setup.deviceregisterdata.ValidationPreview',
        'setup.deviceregisterdata.RegisterTopFilter'
    ],

    models: [
    ],

    stores: [
        'RegisterData',
        'NumericalRegisterData',
        'BillingRegisterData',
        'TextRegisterData',
        'FlagsRegisterData',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.store.RegisterDataDurations'
    ],

    refs: [
        { ref: 'page', selector: 'deviceRegisterDataPage' },
        { ref: 'deviceregisterreportpreview', selector: '#deviceregisterreportpreview' },
        {
            ref: 'filterPanel',
            selector: 'deviceRegisterDataPage mdc-registers-topfilter'
        },
        {
            ref: 'stepsMenu',
            selector: '#stepsMenu'
        }
    ],

    registerBeingViewed: null,
    unitOfMeasureCalculated: '',

    init: function () {
        var me = this;

        me.control({
            '#deviceregisterreportsetup #deviceregisterreportgrid': {
                select: me.loadGridItemDetail
            },
            'deviceregisterdataactionmenu': {
                beforeshow: this.checkSuspect,
                click: this.chooseAction
            }
        });
    },

    loadGridItemDetail: function (rowmodel, record) {
        this.getDeviceregisterreportpreview().updateContent(record);
    },

    showDeviceRegisterDataView: function (deviceId, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerModel = me.getModel('Mdc.model.Register'),
            router = me.getController('Uni.controller.history.Router'),
            registersStore = me.getStore('Mdc.store.RegisterConfigsOfDevice'),
            dependenciesCount = 3,
            device,
            onDependenciesLoad = function () {
                dependenciesCount--;
                if (!dependenciesCount) {
                    var widget = Ext.widget('tabbedDeviceRegisterView', {
                            device: device,
                            router: router
                        }),
                        type = registerBeingViewed.get('type'),
                        collectedReadingType = registerBeingViewed.get('readingType'),
                        collectedUnit = collectedReadingType.names.unitOfMeasure,
                        dataReport = Ext.widget('deviceregisterreportsetup-' + type, {
                            deviceId: deviceId,
                            registerId: registerId,
                            useMultiplier: registerBeingViewed.get('useMultiplier'),
                            unitOfMeasureCollected: collectedUnit,
                            mentionDataLoggerSlave: !Ext.isEmpty(device.get('isDataLogger')) && device.get('isDataLogger'),
                            router: router
                        }),
                        dataStore = me.getStore(type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');

                    dataStore.loadData([], false);

                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#registerTabPanel').setTitle(collectedReadingType.fullAliasName);

                    Ext.suspendLayouts();
                    widget.down('#registerTabPanel').down('#register-data').add(dataReport);
                    tabController.showTab(1);
                    me.getFilterPanel().bindStore(dataStore);
                    Ext.resumeLayouts(true);

                    contentPanel.setLoading(false);
                    dataStore.on('load', me.onDataStoreLoad, me, {single: true});
                    dataStore.load();
                    var applyBtn = contentPanel.down('#filter-apply-all'),
                        clearAllBtn = contentPanel.down('#filter-clear-all');
                    if (applyBtn) {
                        applyBtn.on('click', me.onApplyFilter, me);
                    }
                    if (clearAllBtn) {
                        clearAllBtn.on('click', me.onApplyFilter, me);
                    }
                }
            };

        contentPanel.setLoading();
        registersStore.getProxy().extraParams = {deviceId: deviceId};
        registersStore.load(onDependenciesLoad);
        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                device = record;
                me.getApplication().fireEvent('loadDevice', device);
                onDependenciesLoad();
            }
        });
        registerModel.getProxy().setExtraParam('deviceId', deviceId);
        registerModel.load(registerId, {
            success: function (record) {
                registerBeingViewed = record;
                me.getApplication().fireEvent('loadRegisterConfiguration', registerBeingViewed);
                onDependenciesLoad();
            }
        });
    },

    onApplyFilter: function() {
        var me = this,
            type = registerBeingViewed.get('type'),
            dataStore = me.getStore(type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');
        dataStore.on('load', me.onDataStoreLoad, me, {single: true});
    },

    onDataStoreLoad: function(store, records) {
        var me = this,
            type = registerBeingViewed.get('type'),
            collectedReadingType = registerBeingViewed.get('readingType'),
            collectedUnit = collectedReadingType.names.unitOfMeasure,
            calculatedUnit = 'NY',
            isCumulative = registerBeingViewed.get('isCumulative'),
            multiplier = registerBeingViewed.get('multiplier'),
            hasCalculatedValue = false,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            calculatedValueColumn = contentPanel.down('grid').down('[dataIndex=calculatedValue]'),
            deltaValueColumn = contentPanel.down('grid').down('[dataIndex=deltaValue]'),
            valueColumn = contentPanel.down('grid').down('[dataIndex=value]');

        Ext.Array.each(records, function(record) {
            hasCalculatedValue = hasCalculatedValue || !Ext.isEmpty(record.get('calculatedValue'));
            if (hasCalculatedValue) {
                calculatedUnit = record.get('calculatedUnit');
                return false; // Stop the iteration
            }
        }, me);

        if (valueColumn) {
            valueColumn.setText(Uni.I18n.translate('general.collected', 'MDC', 'Collected') + ' (' + collectedUnit + ')');
        }
        if (calculatedValueColumn) {
            if (hasCalculatedValue) {
                calculatedValueColumn.setText(Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + calculatedUnit + ')');
            }
            calculatedValueColumn.setVisible(hasCalculatedValue);
        }

        if (type === 'billing' || type === 'numerical') {
            if (isCumulative) {
                deltaValueColumn.setText(Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value')
                    + ' (' + (calculatedUnit != 'NY' ? calculatedUnit : collectedUnit) + ')'
                );
                deltaValueColumn.setVisible(true);
            }
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getPage().down('grid'),
            record = grid.getView().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'confirmValue':
                me.getPage().setLoading();
                record.getProxy().setParams(router.arguments.deviceId, router.arguments.registerId);
                record.set('isConfirmed', true);
                record.save({
                    callback: function (rec, operation, success) {
                        if (success) {
                            rec.set('validationResult', 'validationStatus.ok');
                            grid.getView().refreshNode(grid.getStore().indexOf(rec));
                            me.getPage().down('form').loadRecord(rec);
                        }
                        me.getPage().setLoading(false);
                    }
                });
                break;
        }
    },

    checkSuspect: function (menu) {
        var me = this,
            record = me.getPage().down('grid').getView().getSelectionModel().getLastSelected(),
            mainStatus = record.get('validationResult').split('.')[1] == 'suspect',
            bulkStatus = record.get('validationResult').split('.')[1] == 'suspect';

        menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
    }
})
;

