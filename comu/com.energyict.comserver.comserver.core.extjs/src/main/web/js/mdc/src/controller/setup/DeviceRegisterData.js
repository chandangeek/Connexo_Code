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
        'Mdc.store.RegisterDataDurations',
        'Mdc.store.RegisterValidationConfiguration'
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
            },
            'register-data-bulk-action-menu': {
                click: this.chooseBulkAction
            },
        });
    },

    loadGridItemDetail: function (rowmodel, record) {
        this.getDeviceregisterreportpreview().updateContent(record,registerBeingViewed);
    },

    showDeviceRegisterDataView: function (deviceId, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerModel = me.getModel('Mdc.model.Register'),
            router = me.getController('Uni.controller.history.Router'),
            registersStore = me.getStore('Mdc.store.RegisterConfigsOfDevice'),
            validationConfigurationStore = me.getStore('Mdc.store.RegisterValidationConfiguration'),
            dependenciesCount = 4,
            device,
            onDependenciesLoad = function () {
                dependenciesCount--;
                if (!dependenciesCount) {
                    var widget = Ext.widget('tabbedDeviceRegisterView', {
                            device: device,
                            router: router,
                            validationConfigurationStore: validationConfigurationStore
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
                        clearAllBtn = contentPanel.down('#filter-clear-all'),
                        applyDateFilter = me.getFilterPanel().down('#deviceregister-topfilter-interval');
                    if (applyBtn) {
                        applyBtn.on('click', me.onApplyFilter, me);
                    }
                    if(applyDateFilter) {
                        applyDateFilter.on('filterupdate', me.onApplyFilter, me);
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

        validationConfigurationStore.getProxy().extraParams = {deviceId: deviceId, registerId: registerId};
        validationConfigurationStore.load(function () {
            onDependenciesLoad();
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
            isBilling = registerBeingViewed.get('isBilling'),
            hasEvent = registerBeingViewed.get('hasEvent'),
            multiplier = registerBeingViewed.get('multiplier'),
            hasCalculatedValue = false,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            calculatedValueColumn = contentPanel.down('grid').down('[dataIndex=calculatedValue]'),
            deltaValueColumn = contentPanel.down('grid').down('[dataIndex=deltaValue]'),
            valueColumn = contentPanel.down('grid').down('[dataIndex=value]'),
            measurementTimeColumn = contentPanel.down('grid').down('[dataIndex=timeStamp]'),
            intervalTimeColumn = contentPanel.down('grid').down('[dataIndex=interval]'),
            eventTimeColumn = contentPanel.down('grid').down('#eventTime');

        Ext.Array.each(records, function(record) {
            hasCalculatedValue = hasCalculatedValue || !Ext.isEmpty(record.get('calculatedValue'));
            if (hasCalculatedValue) {
                calculatedUnit = record.get('calculatedUnit');
                return false; // Stop the iteration
            }
        }, me);


        if (valueColumn) {
            valueColumn.setText(Uni.I18n.translate('general.collected', 'MDC', 'Collected') + (!Ext.isEmpty(collectedUnit) ? ' (' + collectedUnit + ')' : ''));
        }
        if (calculatedValueColumn) {
            if (hasCalculatedValue) {
                calculatedValueColumn.setText(Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + calculatedUnit + ')');
            }
            calculatedValueColumn.setVisible(hasCalculatedValue);
        }

        if (type === 'numerical') {
            intervalTimeColumn.setVisible(false);
            if(isBilling){
                measurementTimeColumn.setVisible(false);
                intervalTimeColumn.setVisible(true);
            }
            if (isCumulative) {
                deltaValueColumn.setText(Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value')
                    + ' (' + (calculatedUnit != 'NY' ? calculatedUnit : collectedUnit) + ')'
                );
                deltaValueColumn.setVisible(true);
                measurementTimeColumn.setVisible(false);
                intervalTimeColumn.setVisible(true);
            }
            if(!hasEvent){
                eventTimeColumn.setVisible(false);
            }
            if(hasEvent && !isCumulative && !isBilling){
                measurementTimeColumn.setVisible(false);
                intervalTimeColumn.setVisible(false);
                eventTimeColumn.setVisible(true);
            }

        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
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
            case 'viewHistory':
                route = 'devices/device/registers/registerdata/history';
                filterParams = {
                    endInterval: Number(menu.record.get('timeStamp') - 1) + '-' + Number(menu.record.get('timeStamp'))
                };
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
                break;
        }
    },

    checkSuspect: function (menu) {
        var me = this,
            record = me.getPage().down('grid').getView().getSelectionModel().getLastSelected(),
            mainStatus = record.get('validationResult').split('.')[1] == 'suspect',
            bulkStatus = record.get('validationResult').split('.')[1] == 'suspect';

        menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
    },

    chooseBulkAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        switch (item.action) {
            case 'viewHistory':
                route = 'devices/device/registers/registerdata/history';
                var param = {};
                me.getFilterPanel().down('#deviceregister-topfilter-interval').applyParamValue(param);
                filterParams = {
                    endInterval: param.intervalStart.toString() + '-' + param.intervalEnd.toString(),
                    changedDataOnly: 'yes'
                };
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },


})
;

