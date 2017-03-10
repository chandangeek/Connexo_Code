/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceValidationResults', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.ValidationResultsDurations'
    ],
    models: [
        'Mdc.model.Device',
        'Mdc.model.ValidationResultsDataFilter',
        'Mdc.model.ValidationResults',
        'Mdc.model.ValidationResultsDataView',
        'Mdc.model.ValidationRuleSet',
        'Mdc.model.ValidationRuleSetVersion',
        'Mdc.model.ValidationRuleSetVersionRule'
    ],
    stores: [
        'Uni.store.DataIntervalAndZoomLevels',
        'Mdc.store.ValidationResultsDurations',
        'Mdc.store.ValidationResultsRuleSets',
        'Mdc.store.ValidationResultsRules',
        'Mdc.store.ValidationResultsVersions',
        'Mdc.store.ValidationResultsLoadProfiles',
        'Mdc.store.ValidationResultsRegisters',
        'Mdc.store.DeviceValidationResults',
        'Mdc.store.DeviceConfigurationResults'
    ],
    views: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsMainView',
        'Mdc.view.setup.devicevalidationresults.ValidationResultsFilter'
    ],
    refs: [
        //{ref: 'page', selector: '#deviceValidationResultsMainView'},
        {ref: 'mainPage', selector: 'mdc-device-validation-results-main-view'},
        {ref: 'validationResultsTabPanel', selector: '#tab-validation-results'},
        {ref: 'sideFilterForm', selector: '#mdc-device-validation-results-filter'},
        {ref: 'filterPanel', selector: 'mdc-device-validation-results-main-view filter-top-panel'},
        {ref: 'filterDataPanel', selector: 'mdc-device-validation-results-main-view #validation-results-data-filter'},
        {ref: 'validationResultsRulesetForm', selector: '#frm-device-validation-results-ruleset'},
        {ref: 'validationResultsLoadProfileRegisterForm', selector: '#frm-device-validation-results-load-profile-register'},
        {ref: 'configurationViewValidationResultsBrowse', selector: '#con-configuration-view-validation-results-browse'},
        {ref: 'ruleSetGrid', selector: '#rule-set-list'},
        {ref: 'ruleSetVersionGrid', selector: '#rule-set-version-list'},
        {ref: 'ruleSetVersionRuleGrid', selector: '#rule-set-version-rule-list'},
        {ref: 'ruleSetVersionRulePreview', selector: '#rule-set-version-rule-preview'},
        {ref: 'configurationViewValidateNowBtn', selector: 'mdc-device-validation-results-ruleset #btn-configuration-view-validate-now'},
        {ref: 'dataViewValidateNowBtn', selector: 'mdc-device-validation-results-load-profile-register #btn-data-view-validate-now'},
        {ref: 'loadProfileGrid', selector: '#validation-result-load-profile-list'},
        {ref: 'registerGrid', selector: '#validation-result-register-list'},
        {ref: 'configurationViewDataValidated', selector: '#frm-device-validation-results-ruleset #dpl-configuration-view-data-validated'},
        {ref: 'configurationViewValidationResults', selector: '#frm-device-validation-results-ruleset #dpl-configuration-view-validation-results'},
        {ref: 'dataViewDataValidated', selector: '#frm-device-validation-results-load-profile-register #dpl-data-view-data-validated'},
        {ref: 'dataViewValidationResults', selector: '#frm-device-validation-results-load-profile-register #dpl-data-view-validation-results'}

    ],
    deviceId: null,
    veto: false,
    dataValidationLastChecked: null,
    loadProfileDurations: [],
    registerDuration: {},
    validationResultsDataObject: {},
    jsonValidationResultData: null,
    isDefaultFilter: true,

    init: function () {
        var me = this;
        me.control({
            '#tab-validation-results': {
                tabChange: this.changeTab
            },
            '#con-configuration-view-validation-results-browse #rule-set-list': {
                select: this.onRuleSetGridSelectionChange
            },
            '#con-configuration-view-validation-results-browse #rule-set-version-list': {
                select: this.onRuleSetVersionGridSelectionChange
            },
            '#con-configuration-view-validation-results-browse #rule-set-version-rule-list': {
                select: this.onRuleSetVersionRuleGridSelectionChange
            },
            'mdc-device-validation-results-ruleset #btn-configuration-view-validate-now': {
                click: this.validateNow
            },
            'mdc-device-validation-results-load-profile-register #btn-data-view-validate-now': {
                click: this.validateNow
            }
        });
    },

    showDeviceValidationResultsMainView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceId = decodeURIComponent(router.arguments.deviceId),
            activeTab = parseInt(router.arguments.activeTab),
            viewport = Ext.ComponentQuery.query('#contentPanel')[0],
            validationResultsStore = me.getStore('Mdc.store.DeviceValidationResults'),
            configurationResultsStore = me.getStore('Mdc.store.DeviceConfigurationResults'),
            loadProfileStore = Ext.getStore('Mdc.store.LoadProfilesOfDevice'),
            loadProfileStoreProxy = loadProfileStore.getProxy(),
            proxyParams = {
                pageParam: loadProfileStoreProxy.pageParam,
                startParam: loadProfileStoreProxy.startParam,
                limitParam: loadProfileStoreProxy.limitParam
            };

        me.deviceId = deviceId;
        viewport.setLoading();
        validationResultsStore.getProxy().setExtraParam('deviceId', deviceId);
        configurationResultsStore.getProxy().setExtraParam('deviceId', deviceId);
        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                if (record.get('hasLogBooks')
                    || record.get('hasLoadProfiles')
                    || record.get('hasRegisters')) {
                    var widget = me.getMainPage();

                    me.getApplication().fireEvent('loadDevice', record);

                    if (!widget) {
                        widget = Ext.widget('mdc-device-validation-results-main-view', {
                            router: router,
                            device: record
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                    } else {
                        widget.device = record;
                        widget.router = router;
                    }
                    me.veto = true;
                    me.getValidationResultsTabPanel().setActiveTab(activeTab);
                    loadProfileStoreProxy.setExtraParam('deviceId', deviceId);
                    loadProfileStoreProxy.pageParam = false;
                    loadProfileStoreProxy.startParam = false;
                    loadProfileStoreProxy.limitParam = false;
                    loadProfileStore.load(function () {
                        switch (activeTab) {
                            case 0:
                                configurationResultsStore.load();
                                break;
                            case 1:
                                validationResultsStore.load();
                                break;
                        }
                    });
                    loadProfileStoreProxy.pageParam = proxyParams.pageParam;
                    loadProfileStoreProxy.startParam = proxyParams.startParam;
                    loadProfileStoreProxy.limitParam = proxyParams.limitParam;
                    me.veto = false;
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
                viewport.setLoading(false);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    changeTab: function (tabPanel, tab) {
        if (!this.veto) {

            var me = this,
                router = me.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};

            if (tab.itemId === 'validationResults-configuration') {
                Ext.util.History.removeListener('change', me.getMainPage().down('deviceMenu').checkNavigation, me.getMainPage().down('deviceMenu'));
                routeParams.deviceId = encodeURIComponent(me.deviceId);
                route = 'devices/device/validationresultsconfiguration';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, router.queryParams);
            }
            else if (tab.itemId === 'validationResults-data') {
                routeParams.deviceId = encodeURIComponent(me.deviceId);
                route = 'devices/device/validationresultsdata';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, router.queryParams);
            }


        } else {
            this.veto = false;
        }
    },

    setDefaults: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            intervalStart = (new Date()).setHours(0, 0, 0, 0),
            durationsStore = me.getStore('Mdc.store.ValidationResultsDurations'),
            firstItem = durationsStore.first();

        router.filter.beginEdit();
        router.filter.set('intervalStart', moment(intervalStart).toDate());
        router.filter.set('duration', '1years');
        router.filter.endEdit();

        me.isDefaultFilter = true;
    },

    getIntervalStart: function (intervalEnd, item) {
        return moment(intervalEnd).subtract(item.get('timeUnit'), item.get('count')).toDate();
    },

    clearFilter: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;

        delete router.queryParams[this.root];
        activeTab = me.getValidationResultsTabPanel().getActiveTab();
            route = 'devices/device/validationresultsdata';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams);
    },

    validateNow: function () {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('validationResults.validate.confirm', 'MDC', 'Validate'),
                msg: 'message',
                confirmation: function () {
                    me.onValidateNow(this);
                }
            });

        viewport.setLoading();
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validationstatus',
            method: 'GET',
            timeout: 60000,
            success: function (response) {

                viewport.setLoading(false);

                var res = Ext.JSON.decode(response.responseText);
                if (!res.isActive || res.allDataValidated) {
                    return;
                }

                if (res.lastChecked) {
                    me.dataValidationLastChecked = new Date(res.lastChecked);
                } else {
                    me.dataValidationLastChecked = new Date();
                }

                confirmationWindow.insert(1, me.getActivationConfirmationContent());
                confirmationWindow.show({
                    title: Ext.String.format(Uni.I18n.translate('validationResults.validate.title', 'MDC', 'Validate data of device {0}?'), me.deviceId)
                });

            },
            failure: function (record) {
                viewport.setLoading(false);
            }
        });

        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },

    onValidateNow: function (confWindow) {
        var me = this,
            isFromNewValidation = confWindow.down('#rdg-validation-run').getValue().validation === 'newValidation';

        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validationstatus',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: 'true',
                lastChecked: (isFromNewValidation ? confWindow.down('#dtm-validation-from-date').getValue().getTime() : me.dataValidationLastChecked.getTime()),
                device: _.pick(me.getMainPage().device.getRecordData(), 'name', 'version', 'parent')
            },
            success: function () {
                me.updateDevice(function () {
                    me.validateData(confWindow)
                });
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);

                if (response.status === 400) {
                    me.showValidationActivationErrors(res.errors[0].msg);
                    me.confirmationWindowButtonsDisable(false);
                } else {
                    confWindow.destroy();
                }
            }
        });
    },

    validateData: function (confWindow) {
        var me = this;

        confWindow.down('#pnl-validation-progress').add(Ext.create('Ext.ProgressBar', {
                margin: '5 0 15 0'
            })).wait({
                duration: 120000,
                text: Uni.I18n.translate('device.dataValidation.isInProgress', 'MDC', 'Data validation is in progress. Please wait...'),
                fn: function () {
                    me.destroyConfirmationWindow();
                    Ext.widget('messagebox', {
                        buttons: [
                            {
                                text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
                                ui: 'remove',
                                handler: function () {
                                    this.up('window').close();
                                }
                            }
                        ],
                        listeners: {
                            close: function () {
                                this.destroy();
                            }
                        }
                    }).show({
                            ui: 'notification-error',
                            title: Uni.I18n.translate('device.dataValidation.timeout.title1', 'MDC', 'Data validation takes longer than expected'),
                            msg: Uni.I18n.translate('device.dataValidation.timeout.msg1', 'MDC', 'Data validation takes longer than expected. Data validation will continue in the background.'),
                            icon: Ext.MessageBox.ERROR
                        });
                }
            });

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            isNotEdit: true,
            jsonData: _.pick(me.getMainPage().device.getRecordData(), 'name', 'version', 'parent'),
            success: function () {
                me.getStore('Mdc.store.DeviceValidationResults').load();
                me.destroyConfirmationWindow();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('device.dataValidation.activation.validated', 'MDC', 'Data validation completed'));
                me.getController('Uni.controller.history.Router').getRoute().forward();
            },
            failure: function (response) {
                var res;

                if (confWindow) {
                    if (response.status === 400) {
                        res = Ext.JSON.decode(response.responseText);
                        confWindow.down('#pnl-validation-progress').removeAll(true);
                        me.showValidationActivationErrors(res.errors[0].msg);
                        me.confirmationWindowButtonsDisable(false);
                    } else {
                        me.destroyConfirmationWindow();
                    }
                }
            }
        });
    },

    getActivationConfirmationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'rdg-validation-run',
                    columns: 1,
                    padding: '-5 0 0 60',
                    defaults: {
                        name: 'validationRun'
                    },
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('validationResults.validate.fromLast', 'MDC', 'Validate data from last validation'),
                            inputValue: 'lastValidation',
                            itemId: 'rdo-validate-from-last',
                            xtype: 'radiofield',
                            checked: true,
                            name: 'validation'
                        },
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            width: 300,
                            items: [
                                {
                                    boxLabel: Uni.I18n.translate('validationResults.validate.from', 'MDC', 'Validate data from'),
                                    inputValue: 'newValidation',
                                    itemId: 'rdo-validate-from-date',
                                    xtype: 'radiofield',
                                    name: 'validation'
                                },
                                {
                                    xtype: 'datefield',
                                    itemId: 'dtm-validation-from-date',
                                    editable: false,
                                    showToday: false,
                                    value: me.dataValidationLastChecked,
                                    fieldLabel: '  ',
                                    labelWidth: 10,
                                    width: 150,
                                    listeners: {
                                        focus: {
                                            fn: function () {
                                                var radioButton = Ext.ComponentQuery.query('#rdg-validation-run #rdo-validate-from-date')[0];
                                                radioButton.setValue(true);
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    itemId: 'pnl-validation-date-errors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
                {
                    xtype: 'panel',
                    itemId: 'pnl-validation-progress',
                    layout: 'fit',
                    padding: '0 0 0 50'
                }
            ]
        });
    },

    destroyConfirmationWindow: function () {
        var confirmationWindow = Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0];
        if (confirmationWindow) {
            confirmationWindow.removeAll(true);
            confirmationWindow.destroy();
        }
    },

    confirmationWindowButtonsDisable: function (value) {
        var confirmationWindow = Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0];
        if (confirmationWindow) {
            confirmationWindow.down('button[name=confirm]').setDisabled(value);
            confirmationWindow.down('button[name=cancel]').setDisabled(value);
        }
    },

    showValidationActivationErrors: function (errors) {
        var confirmationWindow = Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0];
        if (confirmationWindow) {
            confirmationWindow.down('#pnl-validation-date-errors').update(errors);
            confirmationWindow.down('#pnl-validation-date-errors').setVisible(true);
        }
    },

    onRuleSetGridSelectionChange: function (grid, record) {
        var me = this,
            ruleSetVersionGrid = me.getRuleSetVersionGrid();

        ruleSetVersionGrid.getStore().on('datachanged', function () {
            ruleSetVersionGrid.getSelectionModel().select(0);
            return true;
        }, this);
        ruleSetVersionGrid.getStore().loadData(record.get('detailedRuleSetVersions'));
    },

    onRuleSetVersionGridSelectionChange: function (grid, record) {
        var me = this,
            ruleSetVersionRuleGrid = me.getRuleSetVersionRuleGrid();

        ruleSetVersionRuleGrid.getStore().on('datachanged', function () {
            ruleSetVersionRuleGrid.getSelectionModel().select(0);
            return true;
        }, this);
        ruleSetVersionRuleGrid.getStore().loadData(record.get('detailedRules'));
    },

    onRuleSetVersionRuleGridSelectionChange: function (grid, record) {
        var me = this,
            rulePreview = me.getRuleSetVersionRulePreview(),
            validationRule = record,
            readingTypes = validationRule.data.readingTypes;

        rulePreview.loadRecord(validationRule);
        rulePreview.setTitle(validationRule.get('name'));
        Ext.suspendLayouts();
        rulePreview.down('property-form').removeAll();
        if (validationRule.properties() && validationRule.properties().count()) {
            rulePreview.down('property-form').loadRecord(validationRule);
        }
        rulePreview.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                readingType = readingTypes[i];

            rulePreview.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 260,
                            value: readingType
                        }
                    ]
                }
            );
        }
        Ext.resumeLayouts(true);
    },

    updateDevice: function (callback) {
        var me = this,
            page = me.getMainPage();

        me.getModel('Mdc.model.Device').load(page.device.get('name'), {
            success: function (record) {
                if (page.rendered) {
                    page.device = record;
                    callback.call(me);
                }
            }
        });
    }
});