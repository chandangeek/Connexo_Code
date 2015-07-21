Ext.define('Mdc.controller.setup.DeviceValidationResults', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.ValidationResultsDurations'
    ],
    models: [
        'Mdc.model.ValidationResultsDataFilter',
        'Mdc.model.ValidationResults',
        'Mdc.model.ValidationResultsDataView',
        'Mdc.model.ValidationRuleSet',
        'Mdc.model.ValidationRuleSetVersion',
        'Mdc.model.ValidationRuleSetVersionRule'
    ],
    stores: [
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.ValidationResultsDurations',
        'Mdc.store.ValidationResultsRuleSets',
        'Mdc.store.ValidationResultsRules',
        'Mdc.store.ValidationResultsVersions',
        'Mdc.store.ValidationResultsLoadProfiles',
        'Mdc.store.ValidationResultsRegisters'
    ],
    views: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsMainView'
    ],
    refs: [
        //{ref: 'page', selector: '#deviceValidationResultsMainView'},
        {ref: 'validationResultsTabPanel', selector: '#tab-validation-results'},
        {ref: 'sideFilterForm', selector: '#frm-device-validation-results-filter'},
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
    mRID: null,
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
            '#pnl-device-validation-results-filter': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            },
            '#validation-results-data-filter': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            },
            'mdc-device-validation-results-side-filter #btn-device-validation-results-filter-apply': {
                click: this.applyFilter
            },
            'mdc-device-validation-results-side-filter #btn-device-validation-results-filter-reset': {
                click: this.clearFilter
            },
            '#con-configuration-view-validation-results-browse #rule-set-list': {
                selectionchange: this.onRuleSetGridSelectionChange
            },
            '#con-configuration-view-validation-results-browse #rule-set-version-list': {
                selectionchange: this.onRuleSetVersionGridSelectionChange
            },
            '#con-configuration-view-validation-results-browse #rule-set-version-rule-list': {
                selectionchange: this.onRuleSetVersionRuleGridSelectionChange
            },
            'mdc-device-validation-results-ruleset #btn-configuration-view-validate-now': {
                click: this.validateNow
            },
            'mdc-device-validation-results-load-profile-register #btn-data-view-validate-now': {
                click: this.validateNow
            }

        });

        this.callParent();
    },

    showDeviceValidationResultsMainView: function (mRID, ruleSetId, versionId, ruleId, activeTab) {

        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        activeTab = activeTab || ruleId || versionId || ruleSetId;

        me.mRID = mRID;
        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);

                var widget = Ext.widget('mdc-device-validation-results-main-view', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.veto = true;
                me.getValidationResultsTabPanel().setActiveTab(activeTab);
                if (activeTab == 0)
                    me.loadConfigurationData();
                else
                    me.loadValidationResultsData();
                me.veto = false;
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
                routeParams.mRID = encodeURIComponent(me.mRID);
                route = 'devices/device/validationresultsconfiguration';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, router.queryParams);
            }
            else if (tab.itemId === 'validationResults-data') {
                routeParams.mRID = encodeURIComponent(me.mRID);
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

    setFilterView: function () {
        var me = this,
            filterForm = me.getSideFilterForm(),
            filterView = me.getFilterPanel(),
            filterDataView = me.getFilterDataPanel(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalStart = intervalStartField.getValue(),
            eventDateText = '';

        if (!me.isDefaultFilter) {
            eventDateText += Uni.DateTime.formatDateShort(intervalStart);
            filterView.setFilter('eventDateChanged', Uni.I18n.translate('validationResults.intervallabel', 'MDC', 'From'), eventDateText, true);
            filterDataView.setFilter('eventDateChanged', Uni.I18n.translate('validationResults.intervallabel', 'MDC', 'From'), eventDateText, true);
        }

        filterView.down('#Reset').setText(Uni.I18n.translate('general.reset', 'MDC', 'Reset'));
        filterDataView.down('#Reset').setText(Uni.I18n.translate('general.reset', 'MDC', 'Reset'));
        me.setFilterDataView();
    },

    setFilterDataView: function () {
        var me = this,
            filterForm = me.getSideFilterForm(),
            filterDataView = me.getFilterDataPanel(),
            itemTypeContainer = filterForm.down('[name=itemTypeContainer]'),
            router = me.getController('Uni.controller.history.Router'),
            ruleSetId = router.arguments['ruleSetId'],
            versionId = router.arguments['ruleSetVersionId'],
            ruleId = router.arguments['ruleId'],
            itemName, model;

        if (ruleId) {
            itemTypeContainer.setFieldLabel(Uni.I18n.translate('validationResults.rule', 'MDC', 'Validation rule'));
            model = me.getModel('Mdc.model.ValidationRuleSetVersionRule');
            model.getProxy().setUrl(ruleSetId, versionId, ruleId);

            model.load('', {
                success: function (record) {
                    itemName = record.get('name');
                    filterDataView.setFilter('itemTypeContainer', filterForm.down('#fco-item-type').getFieldLabel(), itemName);

                }
            });
        }
        else if (versionId) {
            itemTypeContainer.setFieldLabel(Uni.I18n.translate('validationResults.ruleSet.version', 'MDC', 'Validation rule set version'));
            model = me.getModel('Mdc.model.ValidationRuleSetVersion');
            model.getProxy().setUrl(ruleSetId, versionId);

            model.load('', {
                success: function (record) {
                    itemName = record.get('name');
                    filterDataView.setFilter('itemTypeContainer', filterForm.down('#fco-item-type').getFieldLabel(), itemName);
                }
            });
        }
        else if (ruleSetId) {
            itemTypeContainer.setFieldLabel(Uni.I18n.translate('validationResults.ruleSet', 'MDC', 'Validation rule set'));
            model = me.getModel('Mdc.model.ValidationRuleSet');
            model.getProxy().setUrl(ruleSetId);

            model.load('', {
                success: function (record) {
                    itemName = record.get('name');
                    filterDataView.setFilter('itemTypeContainer', filterForm.down('#fco-item-type').getFieldLabel(), itemName);
                }
            });
        }

    },

    clearFilter: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;

        delete router.queryParams[this.root];
        activeTab = me.getValidationResultsTabPanel().getActiveTab();
        if (activeTab.itemId === 'validationResults-data') {
            route = 'devices/device/validationresultsdata';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams);
        }
        else
            this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            record = router.filter,
            routeParams = router.arguments;

        if (key === 'itemTypeContainer') {
            delete router.queryParams[this.root];
            route = 'devices/device/validationresultsdata';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams);
            record.set(key, false);
        }
        me.isDefaultFilter = true;
    },

    applyFilter: function () {
        var me = this,
            filterForm = this.getSideFilterForm();

        filterForm.updateRecord();
        filterForm.getRecord().save();
        me.isDefaultFilter = false;
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
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
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
                    title: Ext.String.format(Uni.I18n.translate('validationResults.validate.title', 'MDC', 'Validate data of device {0}?'), me.mRID)
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

        var me = this;

        var isFromLastValidation = confWindow.down('#rdg-validation-run').getValue().validation === 'lastValidation';
        var isFromNewValidation = confWindow.down('#rdg-validation-run').getValue().validation === 'newValidation';

        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'true',
                lastChecked: (isFromNewValidation ? confWindow.down('#dtm-validation-from-date').getValue().getTime() : me.dataValidationLastChecked.getTime())
            },
            success: function () {
                me.validateData(confWindow);
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.showValidationActivationErrors(res.errors[0].msg);
                me.confirmationWindowButtonsDisable(false);
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
                            title: Uni.I18n.translate('device.dataValidation.timeout.title', 'MDC', 'Data validation takes longer as expected'),
                            msg: Uni.I18n.translate('device.dataValidation.timeout.msg', 'MDC', 'Data validation takes longer as expected. Data validation will continue in the background'),
                            icon: Ext.MessageBox.ERROR
                        });
                }
            });

        Ext.Ajax.suspendEvent('requestexception');

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            success: function () {
                me.destroyConfirmationWindow();
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation completed'));

                me.showDeviceValidationResultsMainView(me.mRID, 0);
            },
            failure: function (response) {
                if (confWindow) {
                    var res = Ext.JSON.decode(response.responseText);
                    confWindow.down('#pnl-validation-progress').removeAll(true);
                    me.showValidationActivationErrors(res.errors[0].msg);
                    me.confirmationWindowButtonsDisable(false);
                }
            },
            callback: function () {
                Ext.Ajax.resumeEvent('requestexception');
            }
        });
    },

    getActivationConfirmationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left'
            },
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'rdg-validation-run',
                    columns: 1,
                    padding: '-10 0 0 60',
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
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].removeAll(true);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].destroy();
        }
    },

    confirmationWindowButtonsDisable: function (value) {
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('button[name=confirm]').setDisabled(value);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('button[name=cancel]').setDisabled(value);
        }
    },

    showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('#pnl-validation-date-errors').update(errors);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('#pnl-validation-date-errors').setVisible(true);
        }
    },

    loadConfigurationData: function () {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            models = me.getModel('Mdc.model.ValidationResults'),
            router = me.getController('Uni.controller.history.Router');

        if (Ext.isEmpty(router.filter.data.intervalStart)) {
            me.setDefaults();
        } else {
            me.isDefaultFilter = false;
        }
        me.getSideFilterForm().loadRecord(router.filter);
        me.setFilterView();

        var updatingStatus = Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...');
        me.getConfigurationViewDataValidated().setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
        me.getConfigurationViewValidationResults().setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));


        models.getProxy().setUrl(me.mRID);
        models.getProxy().setFilterParameters(me.jsonValidationResultData);
        models.getProxy().setFilterModel(router.filter);

        viewport.setLoading();
        models.load('', {
            success: function (record) {
                me.loadConfigurationDataItems(record);
                viewport.setLoading(false);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    loadConfigurationDataItems: function (record) {
        var me = this,
            validationResultsRulesetForm = me.getValidationResultsRulesetForm();
        ruleSetGrid = me.getRuleSetGrid(),
            ruleSetVersionGrid = me.getRuleSetVersionGrid(),
            ruleSetVersionRuleGrid = me.getRuleSetVersionRuleGrid(),
            router = me.getController('Uni.controller.history.Router');

        validationResultsRulesetForm.loadRecord(record);
        ruleSetGrid.router = router;
        ruleSetVersionGrid.router = router;
        ruleSetVersionRuleGrid.router = router;

        var configurationViewValidationResultsBrowse = me.getConfigurationViewValidationResultsBrowse();
        configurationViewValidationResultsBrowse.setVisible(record.get('detailedRuleSets') && record.get('detailedRuleSets').length > 0);

        var configurationViewValidateNowBtn = me.getConfigurationViewValidateNowBtn();
        !!configurationViewValidateNowBtn && configurationViewValidateNowBtn.setDisabled(!record.get('isActive') || record.get('allDataValidated'));

        ruleSetGrid.getStore().on('datachanged', function () {
            ruleSetGrid.getSelectionModel().select(0);
            return true;
        }, this);
        ruleSetGrid.getStore().loadData(record.get('detailedRuleSets'));
    },

    loadValidationResultsData: function () {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            models = me.getModel('Mdc.model.ValidationResultsDataView'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            loadProfilesList = [],
            router = me.getController('Uni.controller.history.Router'),
            filterForm = me.getSideFilterForm(),
            intervalStartField = filterForm.down('[name=intervalStart]');

        if (Ext.isEmpty(router.filter.data.intervalStart)) {
            me.setDefaults();
        } else {
            me.isDefaultFilter = false;
        }

        me.getSideFilterForm().loadRecord(router.filter);
        me.setFilterView();

        var updatingStatus = Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...');
        me.getDataViewDataValidated().setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
        me.getDataViewValidationResults().setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));


        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/loadprofiles',
            method: 'GET',
            timeout: 60000,
            success: function (response) {

                viewport.setLoading(false);
                var res = Ext.JSON.decode(response.responseText);


                // for load profiles
                me.loadProfileDurations = [];
                Ext.Array.each(res.loadProfiles, function (loadProfile) {
                    me.loadProfileDurations[me.loadProfileDurations.length] = {
                        id: loadProfile.id,
                        interval: loadProfile.interval,
                        intervalInMs: zoomLevelsStore.getIntervalInMs(zoomLevelsStore.getIntervalRecord(loadProfile.interval).get('all')),
                        intervalRecord: zoomLevelsStore.getIntervalRecord(loadProfile.interval)

                    };
                    loadProfilesList.push({
                        id: loadProfile.id,
                        intervalStart: (me.isDefaultFilter)
                            ? moment(new Date()).valueOf() - zoomLevelsStore.getIntervalInMs(zoomLevelsStore.getIntervalRecord(loadProfile.interval).get('all'))
                            : moment(intervalStartField.getValue()).valueOf(),
                        intervalEnd: (me.isDefaultFilter)
                            ? moment(new Date()).valueOf()
                            : moment(intervalStartField.getValue()).valueOf() + zoomLevelsStore.getIntervalInMs(zoomLevelsStore.getIntervalRecord(loadProfile.interval).get('all'))
                    })
                });

                // for registers
                me.registerDuration.interval = {count: 1, timeUnit: 'years'};
                me.registerDuration.intervalInMs = zoomLevelsStore.getIntervalInMs(me.registerDuration.interval);
                me.registerDuration.intervalRecord = zoomLevelsStore.getIntervalRecord(me.registerDuration.interval);

                me.validationResultsDataObject = {
                    loadProfiles: loadProfilesList
                };

                me.jsonValidationResultData = Ext.encode(loadProfilesList);

                models.getProxy().setUrl(me.mRID);
                models.getProxy().setFilterParameters(me.jsonValidationResultData);
                models.getProxy().setFilterModel(router.filter, me.isDefaultFilter);

                viewport.setLoading();
                models.load('', {
                    success: function (record) {
                        me.loadValidationResultsDataItems(record);
                        viewport.setLoading(false);
                    },
                    failure: function (response) {
                        viewport.setLoading(false);
                    }
                });

            },
            failure: function (record) {
                viewport.setLoading(false);
            }
        });
    },

    loadValidationResultsDataItems: function (record) {

        var me = this,
            validationResultsDataForm = me.getValidationResultsLoadProfileRegisterForm();
        loadProfileGrid = me.getLoadProfileGrid(),
            registerGrid = me.getRegisterGrid(),
            validationResultsDataForm.loadRecord(record),
            router = me.getController('Uni.controller.history.Router'),
            detailedValidationLoadProfile = record.get('detailedValidationLoadProfile'),
            detailedValidationRegister = record.get('detailedValidationRegister'),
            filterForm = me.getSideFilterForm(),
            intervalStartField = filterForm.down('[name=intervalStart]');

        loadProfileGrid.router = router;
        Ext.Array.each(detailedValidationLoadProfile, function (loadProfile) {

            Ext.Array.each(me.loadProfileDurations, function (loadProfileDuration) {
                if (loadProfile.id == loadProfileDuration.id) {
                    loadProfile.interval = me.isDefaultFilter ? null : loadProfileDuration.interval;
                    loadProfile.intervalInMs = loadProfileDuration.intervalInMs;
                    loadProfile.intervalRecord = loadProfileDuration.intervalRecord;

                    loadProfile.intervalStart = (me.isDefaultFilter)
                        ? moment(new Date()).valueOf() - loadProfileDuration.intervalInMs
                        : moment(intervalStartField.getValue()).valueOf();
                    loadProfile.intervalEnd = (me.isDefaultFilter)
                        ? moment(new Date()).valueOf()
                        : moment(intervalStartField.getValue()).valueOf() + loadProfileDuration.intervalInMs;


                    return;
                }

            });
        });
        loadProfileGrid.getStore().loadData(detailedValidationLoadProfile);
        loadProfileGrid.setVisible(record.get('detailedValidationLoadProfile') && record.get('detailedValidationLoadProfile').length > 0);
        loadProfileGrid.getSelectionModel().select(0);


        registerGrid.router = router;
        Ext.Array.each(detailedValidationRegister, function (register) {
            register.interval = me.isDefaultFilter ? null : me.registerDuration.interval;
            register.intervalInMs = me.registerDuration.intervalInMs;
            register.intervalRecord = me.registerDuration.intervalRecord;

            register.intervalStart = (me.isDefaultFilter)
                ? moment(new Date()).valueOf() - me.registerDuration.intervalInMs
                : moment(intervalStartField.getValue()).valueOf();
            register.intervalEnd = (me.isDefaultFilter)
                ? moment(new Date()).valueOf()
                : moment(intervalStartField.getValue()).valueOf() + me.registerDuration.intervalInMs;


        });
        registerGrid.getStore().loadData(detailedValidationRegister);
        registerGrid.setVisible(record.get('detailedValidationRegister') && record.get('detailedValidationRegister').length > 0);
        registerGrid.getSelectionModel().select(0);


        var dataViewValidateNowBtn = me.getDataViewValidateNowBtn();
        !!dataViewValidateNowBtn && dataViewValidateNowBtn.setDisabled(!record.get('isActive') || record.get('allDataValidated'));
    },

    onRuleSetGridSelectionChange: function (grid, record) {
        var me = this,
            ruleSetVersionGrid = me.getRuleSetVersionGrid();

        ruleSetVersionGrid.getStore().on('datachanged', function () {
            ruleSetVersionGrid.getSelectionModel().select(0);
            return true;
        }, this);
        ruleSetVersionGrid.getStore().loadData(record[0].get('detailedRuleSetVersions'));
    },

    onRuleSetVersionGridSelectionChange: function (grid, record) {
        var me = this,
            ruleSetVersionRuleGrid = me.getRuleSetVersionRuleGrid();

        ruleSetVersionRuleGrid.getStore().on('datachanged', function () {
            ruleSetVersionRuleGrid.getSelectionModel().select(0);
            return true;
        }, this);
        ruleSetVersionRuleGrid.getStore().loadData(record[0].get('detailedRules'));
    },

    onRuleSetVersionRuleGridSelectionChange: function (grid, record) {
        var me = this,
            rulePreview = me.getRuleSetVersionRulePreview(),
            validationRule = record[0],
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
    }

});