/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DataLoggerSlaves', {
    extend: 'Ext.app.Controller',
    requires: ['Mdc.util.LinkPurpose',
               'Mdc.widget.DeviceConfigurationField'
    ],

    LINK_NEW_DATALOGGER_SLAVE : 0,
    LINK_EXISTING_DATALOGGER_SLAVE : 1,
    LINK_MULTI_ELEMENT_SLAVE: 2,

    models: [
        'Mdc.model.Device',
        'Mdc.model.DataLoggerSlaveDevice',
        'Mdc.model.DataLoggerSlaveChannel',
        'Mdc.model.DataLoggerSlaveRegister',
        'Mdc.model.Channel',
        'Mdc.model.Register'
    ],

    stores: [
        'Mdc.store.DataLoggerSlaves',
        'Mdc.store.AvailableDataLoggerSlaves',
        'Mdc.store.AvailableDeviceTypes',
        'Mdc.store.AvailableDeviceConfigurations',
        'Mdc.store.Devices',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.store.RegisterConfigsOfDeviceConfig'
    ],

    views: [
        'Uni.view.window.Confirmation',
        'Mdc.view.setup.dataloggerslaves.Setup',
        'Mdc.view.setup.dataloggerslaves.LinkContainer',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlavesLinkWizardStep1',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlaveDeviceAdd',
        'Mdc.view.setup.dataloggerslaves.MultiElementSlavesLinkWizardStep1',
        'Mdc.view.setup.dataloggerslaves.MultiElementSlaveDeviceAdd',
        'Mdc.view.setup.dataloggerslaves.UnlinkWindow'
    ],

    wizardInformation: null,

    refs: [
        {ref: 'wizard', selector: '#mdc-slave-link-wizard'},
        {ref: 'navigationMenu', selector: '#mdc-link-slave-navigation-menu'},
        {ref: 'step1Panel', selector: '#mdc-dataloggerslave-link-wizard-step1'},
        {ref: 'step1PanelForMultiElement', selector: '#mdc-multi-element-slave-link-wizard-step1'},
        {ref: 'step1FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step1-errors'},
        {ref: 'dataLoggerSlaveNameField', selector: '#dataLoggerSlaveDeviceName'},
        {ref: 'multiElementSlaveNameField', selector: '#multiElementSlaveName'},
        {ref: 'dataLoggerSlaveDeviceConfig', selector: '#dataLoggerSlaveDeviceConfiguration'},
        {ref: 'newDataLoggerSlaveForm', selector: '#mdc-datalogger-slave-device-add'},
        {ref: 'newMultiElementSlaveForm', selector: '#mdc-multi-element-slave-device-add'},
        {ref: 'multiElementSlaveDeviceConfig', selector: '#multiElementSlaveDeviceConfiguration'},
        {ref: 'step2FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step2-errors'},
        {ref: 'step3FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step3-errors'},
        {ref: 'step4FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step4-errors'},
        {ref: 'unlinkWindow', selector: 'dataloggerslave-unlink-window'}
    ],

    init: function () {
        this.control({
            '#mdc-link-dataloggerslave-btn': {
                click: this.onLinkDataLoggerSlave
            },
            '#mdc-dataloggerslavesgrid-link-slave-btn': {
                click: this.onLinkDataLoggerSlave
            },
            'dataloggerslave-link-wizard-step1 #mdc-step1-radiogroup': {
                change: this.onStep1OptionChange
            },
            '#mdc-slave-link-wizard button[navigationBtn=true]': {
                click: this.moveTo
            },
            'slave-link-container #mdc-link-slave-navigation-menu': {
                movetostep: this.moveTo
            },
            '#mdc-dataloggerslaves-action-menu': {
                click: this.onActionMenuClicked
            },
            '#mdc-dataloggerslave-unlink-window-unlink': {
                click: this.onUnlinkDataLoggerSlave
            },
            '#mdc-slave-link-wizard button[action=cancel]': {
                click: this.onCancelWizard
            }
        });
    },

    showDataLoggerSlaves: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            slavesStore = me.getStore('Mdc.store.DataLoggerSlaves'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget;

        mainView.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.wizardInformation = {};
                me.wizardInformation.dataLogger = device;
                me.getApplication().fireEvent('loadDevice', device);
                slavesStore.getProxy().setExtraParam('deviceId', device.get('name'));
                widget = Ext.widget('dataLoggerSlavesSetup',
                    { device: device,
                      purpose: Mdc.util.LinkPurpose.forDevice(device),
                      router: router,
                      store:slavesStore });
                me.getApplication().fireEvent('changecontentevent', widget);
                slavesStore.load(function () {
                    mainView.setLoading(false);
                });
            }
        });
    },

    onLinkDataLoggerSlave: function() {
        this.getController('Uni.controller.history.Router').getRoute('devices/device/dataloggerslaves/link').forward();
    },

    showLinkWizard: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        me.wizardInformation = {};
        mainView.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var purpose = Mdc.util.LinkPurpose.forDevice(device),
                    widget = Ext.widget('slave-link-container', {
                    router: router,
                    service: me.service,
                    returnLink: router.getRoute('devices/device/dataloggerslaves').buildUrl(),
                    purpose: purpose
                });
                // set the model for the new slave (device)
                var slaveDevice = Ext.create('Mdc.model.Device'),
                    newDataLoggerSlaveForm = me.getNewDataLoggerSlaveForm(),
                    newMultiElementSlaveForm = me.getNewMultiElementSlaveForm();
                if (!Ext.isEmpty(newDataLoggerSlaveForm)){
                    newDataLoggerSlaveForm.dataLogger = device;
                    newDataLoggerSlaveForm.loadRecord(slaveDevice);
                }
                if (!Ext.isEmpty(newMultiElementSlaveForm)){
                    newMultiElementSlaveForm.dataLogger = device;
                    newMultiElementSlaveForm.loadRecord(slaveDevice);
                }

                me.wizardInformation = {};
                me.wizardInformation.noData = true;
                me.wizardInformation.dataLogger = device;
                me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').push(Ext.create('Mdc.model.DataLoggerSlaveDevice'));

                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('linkSlave', purpose);
                me.getApplication().fireEvent('changecontentevent', widget);
                mainView.setLoading(false);
            }
        });
    },
    onCancelWizard: function() {
        this.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').pop();
    },

    onStep1OptionChange: function(radioGroup, newValue) {
        var me = this,
            selectExistingSlave = newValue.useExisting,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            step1Panel = me.getStep1Panel(),
            newDataLoggerSlaveForm = me.getNewDataLoggerSlaveForm(),
            slaveCombo = step1Panel.down('#mdc-step1-slave-combo'),
            masterDevice = me.wizardInformation.dataLogger;

        me.getStep1FormErrorMessage().hide();
        if (selectExistingSlave) {
            slaveCombo.setDisabled(false);
            slaveCombo.clearInvalid();
            newDataLoggerSlaveForm.hide()
        } else { // use a new (to be created) data logger slave
            if (Ext.isEmpty(me.getStep1PanelForMultiElement())) {
                slaveCombo.setDisabled(true);
                mainView.setLoading();
                var deviceTypeStore = me.getStore('Mdc.store.AvailableDeviceTypes');
                deviceTypeStore.clearFilter(true);
                deviceTypeStore.filter([
                    Ext.create('Ext.util.Filter', {filterFn: Mdc.util.LinkPurpose.forDevice(masterDevice).deviceTypeFilter})
                ]);
                deviceTypeStore.load(function () {
                    mainView.setLoading(false);
                    newDataLoggerSlaveForm.show();
                });
            // multi-element slave
            }else{
                var nameField = me.getMultiElementSlaveNameField();
                if (!Ext.isEmpty(nameField)){
                    nameField.clearInvalid();
                }
                var multiElementSlaveDeviceConfigField = me.getMultiElementSlaveDeviceConfig();
                if (!Ext.isEmpty(multiElementSlaveDeviceConfigField)) {
                    multiElementSlaveDeviceConfigField.clearInvalid();
                }
            }
        }
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getWizard().getLayout(),
            currentStep = wizardLayout.getActiveItem().navigationIndex,
            direction,
            nextStep,
            changeStep = function () {
                Ext.suspendLayouts();
                me.prepareNextStep(nextStep);
                wizardLayout.setActiveItem(nextStep - 1);
                me.getNavigationMenu().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            };

        if (button.action === 'step-next' || button.action === 'confirm-action') {
            direction = 1;
            nextStep = currentStep + direction;
        } else {
            direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        if (direction > 0) {
            me.validateCurrentStep(currentStep, changeStep);
        } else {
            changeStep();
        }
    },

    validateCurrentStep: function (stepNumber, callback) {
        var me = this,
            doCallback = function () {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        switch (stepNumber) {
            case 1:
                me.validateStep1(function() {
                    me.prepareStep2();
                    doCallback();
                });
                break;
            case 2:
                me.validateStep2(function() {
                    me.prepareStep3();
                    doCallback();
                });
                break;
            case 3:
                me.validateStep3(function() {
                    me.prepareStep4();
                    doCallback();
                });
                break;
            case 4:
                me.validateStep4(function() {
                    me.prepareStep5();
                    doCallback();
                });
                break;
            case 5:
                doCallback();
                break;
            default:
                doCallback();
        }
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getWizard(),
            navigationMenu = me.getNavigationMenu(),
            buttons = wizard.getDockedComponent('mdc-dataloggerslave-link-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm-action]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        switch (stepNumber) {
            case 1:
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                me.getStep2FormErrorMessage().hide();
                nextBtn.show();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                me.getStep3FormErrorMessage().hide();
                nextBtn.show();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 4:
                me.getStep4FormErrorMessage().hide();
                nextBtn.show();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 5:
                nextBtn.hide();
                backBtn.show();
                backBtn.enable();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 6:
                navigationMenu.jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.hide();
                me.linkTheSlave(finishBtn);
                break;
        }
    },

    validateStep1: function (callback) {
        var me = this,
            wizard = me.getWizard(),
            step1ErrorMsg = me.getStep1FormErrorMessage(),
            step1RadioGroup = wizard.down('#mdc-step1-radiogroup'),
            endMethod = function() {
                step1ErrorMsg.hide();
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };
        // Linking datalogger slave to data logger
        if (Ext.isEmpty(me.getStep1PanelForMultiElement())) {
            if (step1RadioGroup.getValue().useExisting) {
                var slaveCombo = me.getStep1Panel().down('#mdc-step1-slave-combo'),
                    slaveDevice = slaveCombo.getStore().getAt(slaveCombo.getStore().findExact('name', slaveCombo.getValue()));

                slaveCombo.allowBlank = false;
                if (!slaveCombo.validate()) {
                    step1ErrorMsg.show();
                } else {
                    slaveCombo.allowBlank = true;
                    me.clearPreviousWizardInfoWhenNeeded(slaveDevice.get('deviceTypeId'), slaveDevice.get('deviceConfigurationId'));
                    me.wizardInformation.useExisting = me.LINK_EXISTING_DATALOGGER_SLAVE;
                    me.wizardInformation.minimalLinkingDates = [];
                    me.wizardInformation.minimalLinkingDates.push(me.wizardInformation.dataLogger.get('shipmentDate'));
                    me.wizardInformation.minimalLinkingDates.push(slaveDevice.get('shipmentDate'));
                    me.wizardInformation.minimalLinkingDates.push(slaveDevice.get('unlinkingTimeStamp'));

                    me.updateWizardInformation(me.addSlaveToMasterDeviceModel(slaveDevice));
                    endMethod();
                }
                return;
            }
            var dataLoggerSlaveNameField = me.getDataLoggerSlaveNameField(),
                name = dataLoggerSlaveNameField.getValue(),
                nameValid = dataLoggerSlaveNameField.validate(),
                deviceConfigurationWidget = me.getDataLoggerSlaveDeviceConfig(),
                deviceConfigurationValid = deviceConfigurationWidget.validate(),
                slaveDeviceType = deviceConfigurationWidget.getDeviceType(),
                formRecord = me.getNewDataLoggerSlaveForm().getRecord();
                checkName = function () {
                    wizard.setLoading();
                    me.getStore('Mdc.store.Devices').load({
                        params: {
                            filter: Ext.encode([{
                                property: 'name',
                                value: name
                            }])
                        },
                        callback: function (records) {
                            wizard.setLoading(false);
                            if (!records.length) {
                                me.clearPreviousWizardInfoWhenNeeded(formRecord.get('deviceTypeId'), formRecord.get('deviceConfigurationId'));
                                me.wizardInformation.useExisting = (slaveDeviceType.isDataLoggerSlave() ? me.LINK_NEW_DATALOGGER_SLAVE : me.LINK_MULTI_ELEMENT_SLAVE);

                                var slaveShipmentDateWithoutSeconds = wizard.down('#mdc-datalogger-slave-device-add #dataLoggerSlaveShipmentDate').getValue().getTime();
                                slaveShipmentDateWithoutSeconds = slaveShipmentDateWithoutSeconds - (slaveShipmentDateWithoutSeconds % 60000);

                                var slaveDeviceModel = me.addSlaveToMasterDeviceModel(formRecord);
                                slaveDeviceModel.id = 0;
                                slaveDeviceModel.deviceTypeName = slaveDeviceType.get('name');
                                slaveDeviceModel.shipmentDate = slaveShipmentDateWithoutSeconds;
                                slaveDeviceModel.version = formRecord.get('version');

                                me.wizardInformation.minimalLinkingDates = [];
                                me.wizardInformation.minimalLinkingDates.push(me.wizardInformation.dataLogger.get('shipmentDate'));
                                me.wizardInformation.minimalLinkingDates.push(slaveDeviceModel.shipmentDate);
                                me.updateWizardInformation(slaveDeviceModel);
                                endMethod();
                            } else {
                                Ext.suspendLayouts();
                                step1ErrorMsg.show();
                                dataLoggerSlaveNameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name must be unique'));
                                Ext.resumeLayouts(true);
                            }
                        }
                    });
                };

            if (!nameValid || !deviceConfigurationValid) {
                step1ErrorMsg.show();
            } else {
                wizard.down('#mdc-datalogger-slave-device-add').updateRecord();
                checkName();
            }
        //linking Multi-element slave
        }else{
            var multiElementSlaveNameField = me.getMultiElementSlaveNameField(),
                multiElementSlaveName = multiElementSlaveNameField.getValue(),
                nameValid = multiElementSlaveNameField.validate(),
                deviceConfigurationWidget = me.getMultiElementSlaveDeviceConfig(),
                deviceConfigurationValid = deviceConfigurationWidget.validate(),
                slaveDeviceType = deviceConfigurationWidget.getDeviceType(),
                newMultiElementForm = me.getNewMultiElementSlaveForm();
            var checkName = function () {
                wizard.setLoading();
                me.getStore('Mdc.store.Devices').load({
                    params: {
                        filter: Ext.encode([{
                            property: 'name',
                            value: multiElementSlaveName
                        }])
                    },
                    callback: function (records) {
                        wizard.setLoading(false);
                        if (!records.length) {
                            newMultiElementForm.updateRecord();
                            var formRecord  = newMultiElementForm.getRecord();

                            me.clearPreviousWizardInfoWhenNeeded(formRecord.get('deviceTypeId'), formRecord.get('deviceConfigurationId'));
                            me.wizardInformation.useExisting = me.LINK_MULTI_ELEMENT_SLAVE;

                            var slaveDeviceModel = me.addSlaveToMasterDeviceModel(formRecord);
                            slaveDeviceModel.id = 0;
                            slaveDeviceModel.deviceTypeName = slaveDeviceType.get('name');
                            slaveDeviceModel.shipmentDate = me.wizardInformation.dataLogger.get('shipmentDate');
                            if (slaveDeviceType.raw.deviceLifeCycleEffectiveTimeShiftPeriod){
                                var start =  slaveDeviceType.raw.deviceLifeCycleEffectiveTimeShiftPeriod.start;
                                var end = slaveDeviceType.raw.deviceLifeCycleEffectiveTimeShiftPeriod.end;
                                if (slaveDeviceModel.shipmentDate < start || slaveDeviceModel.shipmentDate > end){
                                    slaveDeviceModel.shipmentDate = start + 3600000;    // 1 hour to do the input...
                                }
                            }
                            slaveDeviceModel.version = formRecord.get('version');

                            me.wizardInformation.minimalLinkingDates = [];
                            me.wizardInformation.minimalLinkingDates.push(me.wizardInformation.dataLogger.get('shipmentDate'));
                            me.wizardInformation.minimalLinkingDates.push(slaveDeviceModel.shipmentDate);
                            me.updateWizardInformation(slaveDeviceModel);
                            endMethod();
                        } else {
                            Ext.suspendLayouts();
                            step1ErrorMsg.show();
                            multiElementSlaveNameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name must be unique'));
                            Ext.resumeLayouts(true);
                        }
                    }
                });
            };
            if (!nameValid || !deviceConfigurationValid) {
                step1ErrorMsg.show();
            }else {
                checkName();
            }
        }
    },

    addSlaveToMasterDeviceModel: function(slaveDevice){
        var slaveDeviceModel = this.wizardInformation.dataLogger.get('dataLoggerSlaveDevices')[this.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').length - 1];
        slaveDeviceModel.id = slaveDevice.get('id');
        slaveDeviceModel.deviceTypeId = slaveDevice.get('deviceTypeId');
        slaveDeviceModel.deviceTypeName = slaveDevice.get('deviceTypeName');
        slaveDeviceModel.deviceConfigurationId = slaveDevice.get('deviceConfigurationId');
        slaveDeviceModel.deviceConfigurationName = slaveDevice.get('deviceConfigurationName');
        slaveDeviceModel.name = slaveDevice.get('name');
        slaveDeviceModel.serialNumber = slaveDevice.get('serialNumber');
        slaveDeviceModel.manufacturer = slaveDevice.get('manufacturer');
        slaveDeviceModel.modelNbr = slaveDevice.get('modelNbr');
        slaveDeviceModel.modelVersion = slaveDevice.get('modelVersion');
        slaveDeviceModel.shipmentDate = slaveDevice.get('shipmentDate');
        slaveDeviceModel.yearOfCertification = slaveDevice.get('yearOfCertification');
        slaveDeviceModel.batch = slaveDevice.get('batch');
        slaveDeviceModel.version = slaveDevice.get('version');
        return slaveDeviceModel;
    },

    updateWizardInformation: function(slaveDeviceModel){
        this.wizardInformation.slaveName = slaveDeviceModel.name;
        this.wizardInformation.slaveShipmentDate = slaveDeviceModel.shipmentDate;
        this.wizardInformation.slaveDeviceTypeId = slaveDeviceModel.deviceTypeId;
        this.wizardInformation.slaveDeviceConfigurationId = slaveDeviceModel.deviceConfigurationId;
    },

    prepareStep2: function () {
        var me = this,
            wizard = me.getWizard(),
            loadProfileConfigStore = me.getStore('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration');

        wizard.setLoading();
        me.wizardInformation.channelAvailabilityDates = [];  // key = channel id, value is the availability date
        loadProfileConfigStore.getProxy().setUrl(me.wizardInformation.slaveDeviceTypeId, me.wizardInformation.slaveDeviceConfigurationId);
        loadProfileConfigStore.load({
            callback: function (loadProfileConfigRecords) {
                var channelRecords = [];
                Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                    if (dataLoggerSlaveDeviceRecord.id === 0 && dataLoggerSlaveDeviceRecord.dataLoggerSlaveChannelInfos) { // the yet unlinked channels
                        Ext.Array.forEach(dataLoggerSlaveDeviceRecord.dataLoggerSlaveChannelInfos, function(dataLoggerSlaveChannelInfoRecord){
                            channelRecords.push(dataLoggerSlaveChannelInfoRecord.dataLoggerChannel);
                            if ( !Ext.isEmpty(dataLoggerSlaveChannelInfoRecord.availabilityDate) && dataLoggerSlaveChannelInfoRecord.availabilityDate !== 0) {
                                me.wizardInformation.channelAvailabilityDates[dataLoggerSlaveChannelInfoRecord.dataLoggerChannel.id] =
                                    dataLoggerSlaveChannelInfoRecord.availabilityDate;
                            }
                        }, me);
                    }
                }, me);
                me.wizardInformation.loadProfileConfigRecords = loadProfileConfigRecords;
                me.wizardInformation.dataLoggerChannels = channelRecords;
                wizard.setLoading(false);
                wizard.down('dataloggerslave-link-wizard-step2').initialize(
                    loadProfileConfigRecords,
                    channelRecords,
                    me.wizardInformation ? me.wizardInformation.mappedChannels : undefined
                );
            }
        });
    },

    validateStep2: function (callback) {
        // Check if ALL channels of the data logger slave are mapped to DIFFERENT data logger channels
        var me = this,
            wizard = me.getWizard(),
            step2ErrorMsg = me.getStep2FormErrorMessage(),
            i, counter = 0,
            channelCombo,
            errorsFound = false,
            channelsMapped = {},
            mappedChannels = [],
            indicateErrorsFound = function() {
                if (!errorsFound) {
                    errorsFound = true;
                    step2ErrorMsg.show();
                }
            },
            endMethod = function() {
                step2ErrorMsg.hide();
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        step2ErrorMsg.hide();
        for (i=0; true; i++) {
            counter++;
            channelCombo = wizard.down('#mdc-step2-channel-combo-'+counter);
            if (Ext.isEmpty(channelCombo)) {
                break;
            }
            if (Ext.isEmpty(channelCombo.getValue())) {
                indicateErrorsFound();
                channelCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This field is required'));
            } else {
                if (channelCombo.getValue() in channelsMapped) {
                    indicateErrorsFound();
                    channelCombo.markInvalid(Uni.I18n.translate('general.channelAlreadyMapped', 'MDC', 'This field must be unique'));
                    channelsMapped[channelCombo.getValue()].markInvalid(Uni.I18n.translate('general.channelAlreadyMapped', 'MDC', 'This field must be unique'));
                } else {
                    channelsMapped[channelCombo.getValue()] = channelCombo;
                    mappedChannels[counter] = channelCombo.getValue();
                }
            }
        }

        if (!errorsFound) {
            me.wizardInformation.mappedChannels = mappedChannels;

            Ext.Array.forEach(Object.keys(channelsMapped), function(channelId) {
                if (channelId in me.wizardInformation.channelAvailabilityDates) {
                    var availabilityDate = me.wizardInformation.channelAvailabilityDates[channelId];
                    me.wizardInformation.minimalLinkingDates.push(availabilityDate);
                    me.wizardInformation.noData = false;
                }
            }, me);

            var slaveDeviceModel = me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices')[me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').length-1];
            slaveDeviceModel.dataLoggerSlaveChannelInfos = [];
            counter = 0;
            Ext.Array.forEach(me.wizardInformation.loadProfileConfigRecords, function(record) {
                Ext.Array.forEach(record.get('channels'), function(channelSpec) {
                    var slaveChannelModel = Ext.create('Mdc.model.DataLoggerSlaveChannel');
                    slaveChannelModel.slaveChannel = channelSpec;
                    counter++;
                    channelCombo = wizard.down('#mdc-step2-channel-combo-' + counter);
                    var selectedChannelRecord = channelCombo.findRecordByValue(channelCombo.getValue());

                    Ext.Array.forEach(me.wizardInformation.dataLoggerChannels, function(channelRecord) {
                        if (channelRecord.id === selectedChannelRecord.get('id')) {
                            slaveChannelModel.dataLoggerChannel = channelRecord;
                        }
                    }, me);

                    slaveDeviceModel.dataLoggerSlaveChannelInfos.push(slaveChannelModel);
                }, me);
            }, me);
            endMethod();
        }
    },

    prepareStep3: function() {
        var me = this,
            wizard = me.getWizard(),
            registerConfigStore = me.getStore('Mdc.store.RegisterConfigsOfDeviceConfig');

        wizard.setLoading();
        me.wizardInformation.registerAvailabilityDates = [];  // key = register id, value is the availability date
        registerConfigStore.getProxy().setExtraParam('deviceType', me.wizardInformation.slaveDeviceTypeId);
        registerConfigStore.getProxy().setExtraParam('deviceConfig', me.wizardInformation.slaveDeviceConfigurationId);
        registerConfigStore.load({
            callback: function (registerConfigRecords) {
                var registerRecords = [];
                Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                    if (dataLoggerSlaveDeviceRecord.id === 0 && dataLoggerSlaveDeviceRecord.dataLoggerSlaveRegisterInfos) { // yet unlinked registers
                        Ext.Array.forEach(dataLoggerSlaveDeviceRecord.dataLoggerSlaveRegisterInfos, function(dataLoggerSlaveRegisterInfoRecord){
                            registerRecords.push(dataLoggerSlaveRegisterInfoRecord.dataLoggerRegister);
                            if ( !Ext.isEmpty(dataLoggerSlaveRegisterInfoRecord.availabilityDate) && dataLoggerSlaveRegisterInfoRecord.availabilityDate !== 0) {
                                me.wizardInformation.registerAvailabilityDates[dataLoggerSlaveRegisterInfoRecord.dataLoggerRegister.id] =
                                    dataLoggerSlaveRegisterInfoRecord.availabilityDate;
                            }
                        }, me);
                    }
                }, me);
                me.wizardInformation.registerConfigRecords = registerConfigRecords;
                me.wizardInformation.dataLoggerRegisters = registerRecords;
                wizard.setLoading(false);
                wizard.down('dataloggerslave-link-wizard-step3').initialize(
                    registerConfigRecords,
                    registerRecords,
                    me.wizardInformation ? me.wizardInformation.mappedRegisters : undefined
                );
            }
        });
    },

    validateStep3: function(callback) {
        // Check if ALL registers of the slave are mapped to DIFFERENT data logger registers
        var me = this,
            wizard = me.getWizard(),
            step3ErrorMsg = me.getStep3FormErrorMessage(),
            i, counter = 0,
            registerCombo,
            errorsFound = false,
            registersMapped = {},
            mappedRegisters = [],
            indicateErrorsFound = function() {
                if (!errorsFound) {
                    errorsFound = true;
                    step3ErrorMsg.show();
                }
            },
            endMethod = function() {
                step3ErrorMsg.hide();
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        step3ErrorMsg.hide();
        for (i=0; true; i++) {
            counter++;
            registerCombo = wizard.down('#mdc-step3-register-combo-'+counter);
            if (Ext.isEmpty(registerCombo)) {
                break;
            }
            if (Ext.isEmpty(registerCombo.getValue())) {
                indicateErrorsFound();
                registerCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This field is required'));
            } else {
                if (registerCombo.getValue() in registersMapped) {
                    indicateErrorsFound();
                    registerCombo.markInvalid(Uni.I18n.translate('general.registerAlreadyMapped', 'MDC', 'This field must be unique'));
                    registersMapped[registerCombo.getValue()].markInvalid(Uni.I18n.translate('general.registerAlreadyMapped', 'MDC', 'This field must be unique'));
                } else {
                    registersMapped[registerCombo.getValue()] = registerCombo;
                    mappedRegisters[counter] = registerCombo.getValue();
                }
            }
        }

        if (!errorsFound) {
            me.wizardInformation.mappedRegisters = mappedRegisters;

            Ext.Array.forEach(Object.keys(registersMapped), function(registerId) {
                if (registerId in me.wizardInformation.registerAvailabilityDates) {
                    var availabilityDate = me.wizardInformation.registerAvailabilityDates[registerId];
                    me.wizardInformation.minimalLinkingDates.push(availabilityDate);
                    me.wizardInformation.noData = false;
                }
            }, me);

            var slaveDeviceModel = me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices')[me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').length-1];
            slaveDeviceModel.dataLoggerSlaveRegisterInfos = [];
            counter = 0;
            Ext.Array.forEach(me.wizardInformation.registerConfigRecords, function(registerConfigRecord) {
                var slaveRegisterModel = Ext.create('Mdc.model.DataLoggerSlaveRegister');
                slaveRegisterModel.slaveRegister = {};
                slaveRegisterModel.slaveRegister.id = registerConfigRecord.get('id');

                counter++;
                var registerCombo = wizard.down('#mdc-step3-register-combo-' + counter),
                    selectedRegisterRecord = registerCombo.findRecordByValue(registerCombo.getValue());

                Ext.Array.forEach(me.wizardInformation.dataLoggerRegisters, function(dataLoggerRegister) {
                    if (dataLoggerRegister.id === selectedRegisterRecord.get('id')) {
                        slaveRegisterModel.dataLoggerRegister = dataLoggerRegister;
                        slaveRegisterModel.slaveRegister.type = dataLoggerRegister.type;
                    }
                }, me);
                slaveDeviceModel.dataLoggerSlaveRegisterInfos.push(slaveRegisterModel);
            });
            endMethod();
        }
    },

    prepareStep4: function() {
        var me = this,
            wizard4Panel = me.getWizard().down('dataloggerslave-link-wizard-step4'),
            earliestLinkingDate = Ext.isEmpty(me.wizardInformation.minimalLinkingDates) ? 0 : Ext.Array.max(me.wizardInformation.minimalLinkingDates),
            linkingDateToSuggest;

        // Determine the linking date to suggest
        if (me.wizardInformation) {
            if (me.wizardInformation.linkingDate) { // There's one chosen previously
                linkingDateToSuggest = me.wizardInformation.linkingDate;
            } else if (me.wizardInformation.useExisting === me.LINK_EXISTING_DATALOGGER_SLAVE || me.wizardInformation.useExisting === me.LINK_MULTI_ELEMENT_SLAVE) {
                linkingDateToSuggest = earliestLinkingDate;
            } else if (me.wizardInformation.noData) { // No availability dates for the mapped channels/registers
                linkingDateToSuggest = new Date();
            } else {
                linkingDateToSuggest = earliestLinkingDate;
            }
        }
        // To avoid the disabling of certain hours in the linking date picker, we (should) pass midnight:
        var momentOfDate = moment(earliestLinkingDate);
        momentOfDate.startOf('day');
        var earliestLinkingDateMidnight = momentOfDate.unix() * 1000;
        wizard4Panel.initialize(earliestLinkingDateMidnight, linkingDateToSuggest, me.wizardInformation.useExisting !== me.LINK_MULTI_ELEMENT_SLAVE);
        if (me.wizardInformation.useExisting === me.LINK_MULTI_ELEMENT_SLAVE){
            me.wizardInformation.linkingDate = linkingDateToSuggest;
        }
    },

    validateStep4: function(callback) {
        var me = this,
            dateField = me.getWizard().down('#mdc-step4-linking-date'),
            earliestLinkingDate = Ext.isEmpty(me.wizardInformation.minimalLinkingDates) ? 0 : Ext.Array.max(me.wizardInformation.minimalLinkingDates),
            errorMsg,
            endMethod = function() {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        me.wizardInformation.linkingDate = dateField.getValue().getTime() + 59999; // No seconds on the linking date widget

        if (me.wizardInformation.linkingDate < earliestLinkingDate) {
            me.getStep4FormErrorMessage().show();

            if (me.wizardInformation.noData) {
                errorMsg = Uni.I18n.translate('general.linkingDateShouldLieAfterShipmentDateX', 'MDC',
                    'The linking date should be equal to or lie after the shipment date of the data logger ({0})',
                    Uni.DateTime.formatDateTime(new Date(earliestLinkingDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT)
                );
            } else {
                errorMsg = Uni.I18n.translate('general.linkingDateShouldLieAfterReadingDateX', 'MDC',
                    'The linking date should be equal to or lie after the date of the most recent unlinked reading of the channels and registers of the data logger ({0})',
                    Uni.DateTime.formatDateTime(new Date(earliestLinkingDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT)
                );
            }
            me.getWizard().down('#mdc-step4-linking-date').markInvalid(errorMsg);

        } else {
            Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                dataLoggerSlaveDeviceRecord["linkingTimeStamp"] = me.wizardInformation.linkingDate;
            }, me);
            endMethod();
        }
    },

    prepareStep5: function() {
        var me = this;
        me.getWizard().down('dataloggerslave-link-wizard-step5').initialize(me.wizardInformation.dataLogger.get('name'), me.wizardInformation.slaveName);
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'unlinkSlave':
                Ext.widget('dataloggerslave-unlink-window', {
                    title: Uni.I18n.translate('general.unlinkX', 'MDC', "Unlink '{0}'?", menu.record.get('name'), false),
                    dataLoggerSlaveRecord: menu.record
                }).show();
                break;
        }
    },

    linkTheSlave: function(finishBtn) {
        var me = this,
            wizard = me.getWizard(),
            progressbar = wizard.down('#mdc-dataloggerslave-link-wizard-step6-progressbar'),
            infoMessagePanel = wizard.down('dataloggerslave-link-wizard-step6'),
            message;

        Ext.suspendLayouts();
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        infoMessagePanel.update(Uni.I18n.translate('linkSlave.wizard.waitMsg', 'MDC', 'Busy linking the slave. Please wait...'));
        Ext.resumeLayouts(true);

        me.wizardInformation.dataLogger.save({
            success: function (record) {
                message = Ext.String.format(
                    Mdc.util.LinkPurpose.forDevice(me.wizardInformation.dataLogger).slaveLinkedMessage,
                    Ext.String.htmlEncode(me.wizardInformation.slaveName),
                    Ext.String.htmlEncode(me.wizardInformation.dataLogger.get('name'))
                );
                Ext.suspendLayouts();
                infoMessagePanel.update(message);
                finishBtn.show();
                progressbar.hide();
                Ext.resumeLayouts(true);
            },
            failure: function (record, operation) {
                message = Ext.String.format(
                    Mdc.util.LinkPurpose.forDevice(me.wizardInformation.dataLogger).slaveLinkedFailedMessage,
                        Ext.String.htmlEncode(me.wizardInformation.slaveName),
                        Ext.String.htmlEncode(me.wizardInformation.dataLogger.get('name'))
                );
                Ext.suspendLayouts();
                infoMessagePanel.update('');
                infoMessagePanel.add({
                    xtype: 'displayfield',
                    value: message
                });
                finishBtn.show();
                progressbar.hide();
                Ext.resumeLayouts(true);
            }
        });
    },

    clearPreviousWizardInfoWhenNeeded: function(currentTypeId, currentConfigId) {
        if (Ext.isEmpty(this.wizardInformation)) {
            return;
        }

        var me = this,
            thereIsSomethingToClear =
                !Ext.isEmpty(me.wizardInformation.mappedChannels) ||
                !Ext.isEmpty(me.wizardInformation.mappedRegisters) ||
                !Ext.isEmpty(me.wizardInformation.linkingDate) ||
                !Ext.isEmpty(me.wizardInformation.minimalLinkingDates);

        if (!thereIsSomethingToClear) {
            return;
        }

        var configChanged =
                ( !Ext.isEmpty(me.wizardInformation.slaveDeviceTypeId)
                    && me.wizardInformation.slaveDeviceTypeId !== currentTypeId )
                ||
                ( !Ext.isEmpty(me.wizardInformation.slaveDeviceConfigurationId)
                    && me.wizardInformation.slaveDeviceConfigurationId !== currentConfigId );

        if (!configChanged) {
            return;
        }
        this.wizardInformation.mappedChannels = undefined;
        this.wizardInformation.mappedRegisters = undefined;
        this.wizardInformation.linkingDate = undefined;
        this.wizardInformation.minimalLinkingDates = [];
    },

    onUnlinkDataLoggerSlave: function(unlinkButton) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            unlinkWindow = me.getUnlinkWindow(),
            unlinkDate = unlinkWindow.down('#mdc-dataloggerslave-unlink-window-date-picker').getValue(),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            nameOfSlaveToUnlink = unlinkWindow.dataLoggerSlaveRecord.get('name');

        unlinkWindow.close();
        mainView.setLoading();
        Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
            if (dataLoggerSlaveDeviceRecord.name === nameOfSlaveToUnlink) {
                dataLoggerSlaveDeviceRecord['unlinkingTimeStamp'] = unlinkDate.getTime();
            }
        }, me);

        me.wizardInformation.dataLogger.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('general.deviceUnlinked', 'MDC', 'Device unlinked'));
                router.getRoute().forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    }

});

