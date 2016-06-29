Ext.define('Mdc.controller.setup.DataLoggerSlaves', {
    extend: 'Ext.app.Controller',

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
        'Mdc.store.AvailableDataLoggerSlaveDeviceTypes',
        'Mdc.store.Devices',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.store.RegisterConfigsOfDeviceConfig'
    ],

    views: [
        'Uni.view.window.Confirmation',
        'Mdc.view.setup.dataloggerslaves.Setup',
        'Mdc.view.setup.dataloggerslaves.LinkContainer',
        'Mdc.view.setup.device.DeviceAdd',
        'Mdc.view.setup.dataloggerslaves.UnlinkWindow'
    ],

    wizardInformation: null,

    refs: [
        {ref: 'wizard', selector: '#mdc-dataloggerslave-link-wizard'},
        {ref: 'navigationMenu', selector: '#mdc-link-dataloggerslave-navigation-menu'},
        {ref: 'step1Panel', selector: '#mdc-dataloggerslave-link-wizard-step1'},
        {ref: 'step1FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step1-errors'},
        {ref: 'mRIDField', selector: '#deviceAddMRID'},
        {ref: 'deviceTypeCombo', selector: '#deviceAddType'},
        {ref: 'deviceConfigCombo', selector: '#deviceAddConfig'},
        {ref: 'step2FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step2-errors'},
        {ref: 'step3FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step3-errors'},
        {ref: 'step4FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step4-errors'},
        {ref: 'unlinkWindow', selector: 'dataloggerslave-unlink-window'},
        {ref: 'slavesGrid', selector: 'dataLoggerSlavesGrid'}
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
            '#mdc-dataloggerslave-link-wizard button[navigationBtn=true]': {
                click: this.moveTo
            },
            'dataloggerslave-link-container #mdc-link-dataloggerslave-navigation-menu': {
                movetostep: this.moveTo
            },
            '#mdc-dataloggerslaves-action-menu': {
                click: this.onActionMenuClicked
            },
            '#mdc-dataloggerslave-unlink-window-unlink': {
                click: this.onUnlinkDataLoggerSlave
            },
            '#mdc-dataloggerslave-link-wizard button[action=cancel]': {
                click: this.onCancelWizard
            }
        });
    },

    showDataLoggerSlaves: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            slavesStore = me.getStore('Mdc.store.DataLoggerSlaves'),
            widget;

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.wizardInformation = {};
                me.wizardInformation.minimalLinkingDate = device.get('shipmentDate');
                me.wizardInformation.noData = true;
                me.wizardInformation.dataLogger = device;
                me.getApplication().fireEvent('loadDevice', device);
                slavesStore.getProxy().setUrl(device.get('mRID'));
                widget = Ext.widget('dataLoggerSlavesSetup', { device: device, router: router, store:slavesStore });
                me.getApplication().fireEvent('changecontentevent', widget);
                slavesStore.load();
            }
        });
    },

    onLinkDataLoggerSlave: function() {
        this.getController('Uni.controller.history.Router').getRoute('devices/device/dataloggerslaves/link').forward();
    },

    showLinkWizard: function(mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('dataloggerslave-link-container', {
                itemId: 'mdc-dataloggerslave-link-container',
                router: router,
                service: me.service,
                returnLink: router.getRoute('devices/device/dataloggerslaves').buildUrl()
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.wizardInformation = {};
        mainView.setLoading(true);
        widget.down('dataloggerslave-link-wizard').loadRecord(Ext.create('Mdc.model.Device'));
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.wizardInformation.dataLogger = device;
                me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').push(Ext.create('Mdc.model.DataLoggerSlaveDevice'));
                me.getApplication().fireEvent('loadDevice', device);
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
            addDevicePanel = step1Panel.down('#deviceAdd'),
            slaveCombo = step1Panel.down('#mdc-step1-slave-combo');

        me.getStep1FormErrorMessage().hide();
        if (selectExistingSlave) {
            slaveCombo.setDisabled(false);
            slaveCombo.clearInvalid();
            if (!Ext.isEmpty(addDevicePanel)) {
                addDevicePanel.hide();
            }
        } else { // use a new (to be created) data logger slave
            slaveCombo.setDisabled(true);
            if (Ext.isEmpty(addDevicePanel)) {
                var deviceTypeStore = me.getStore('Mdc.store.AvailableDataLoggerSlaveDeviceTypes');
                deviceTypeStore.load(function(records){
                    addDevicePanel = Ext.widget('deviceAdd', { labelWidth: 145, formWidth: 545, deviceTypeStore:deviceTypeStore });
                    addDevicePanel.down('panel').title = '';
                    addDevicePanel.down('#deviceAddMRID').allowBlank = false;
                    addDevicePanel.down('#deviceAddType').allowBlank = false;
                    addDevicePanel.down('#deviceAddConfig').allowBlank = false;
                    addDevicePanel.down('form').items.remove(addDevicePanel.down('#mdc-deviceAdd-btnContainer'));
                    addDevicePanel.down('form').loadRecord(Ext.create('Mdc.model.Device'));
                    step1Panel.add(addDevicePanel);
                    mainView.setLoading();
                    addDevicePanel.down('#deviceAddType').getStore().load(function () {
                        mainView.setLoading(false);
                    });
                });
            } else {
                addDevicePanel.down('#deviceAddMRID').clearInvalid();
                addDevicePanel.down('#deviceAddType').clearInvalid();
                addDevicePanel.down('#deviceAddConfig').clearInvalid();
                addDevicePanel.show();
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

        if (step1RadioGroup.getValue().useExisting) {
            var slaveCombo = me.getStep1Panel().down('#mdc-step1-slave-combo'),
                slaveDevice = slaveCombo.getStore().findRecord('mRID', slaveCombo.getValue());

            slaveCombo.allowBlank = false;
            if (!slaveCombo.validate()) {
                step1ErrorMsg.show();
            } else {
                slaveCombo.allowBlank = true;
                me.clearPreviousWizardInfoWhenNeeded(slaveDevice.get('deviceTypeId'), slaveDevice.get('deviceConfigurationId'));
                me.wizardInformation.useExisting = true;

                var slaveDeviceModel = me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices')[me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').length-1];
                slaveDeviceModel.id = slaveDevice.get('id');
                slaveDeviceModel.mRID = slaveDevice.get('mRID');
                slaveDeviceModel.serialNumber = slaveDevice.get('serialNumber');
                slaveDeviceModel.deviceTypeName = slaveDevice.get('deviceTypeName');
                slaveDeviceModel.deviceConfigurationId = slaveDevice.get('deviceConfigurationId');
                slaveDeviceModel.deviceConfigurationName = slaveDevice.get('deviceConfigurationName');
                slaveDeviceModel.shipmentDate = slaveDevice.get('shipmentDate');
                slaveDeviceModel.yearOfCertification = slaveDevice.get('yearOfCertification');
                //TODO slaveDeviceModel.batch = slaveDevice.get('batch');
                slaveDeviceModel.version = slaveDevice.get('version');

                me.wizardInformation.slaveMRID = slaveDevice.get('mRID');
                me.wizardInformation.slaveShipmentDate = slaveDevice.get('shipmentDate');
                me.wizardInformation.slaveDeviceTypeId = slaveDevice.get('deviceTypeId');
                me.wizardInformation.slaveDeviceConfigurationId = slaveDevice.get('deviceConfigurationId');
                endMethod();
            }
            return;
        }

        var mRIDField = me.getMRIDField(),
            mRIDValid = mRIDField.validate(),
            deviceTypeValid = me.getDeviceTypeCombo().validate(),
            deviceConfigValid = me.getDeviceConfigCombo().validate(),
            mRID = mRIDField.getValue(),
            formRecord = wizard.down('#deviceAdd form').getRecord(),
            checkMRID = function() {
                wizard.setLoading();
                me.getStore('Mdc.store.Devices').load({
                    params: {
                        filter: Ext.encode([{
                            property: 'mRID',
                            value: mRID
                        }])
                    },
                    callback: function (records) {
                        wizard.setLoading(false);
                        if (!records.length) {
                            me.clearPreviousWizardInfoWhenNeeded(formRecord.get('deviceTypeId'), formRecord.get('deviceConfigurationId'));
                            me.wizardInformation.useExisting = false;

                            var slaveDeviceModel = me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices')[me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices').length-1];
                            slaveDeviceModel.id = 0;
                            slaveDeviceModel.mRID = formRecord.get('mRID');
                            slaveDeviceModel.serialNumber = formRecord.get('serialNumber');
                            slaveDeviceModel.deviceTypeName =
                                wizard.down('#deviceAdd #deviceAddType').findRecordByValue(wizard.down('#deviceAdd #deviceAddType').getValue()).get('name');
                            slaveDeviceModel.deviceConfigurationId = formRecord.get('deviceConfigurationId');
                            slaveDeviceModel.yearOfCertification = formRecord.get('yearOfCertification');
                            //TODO slaveDeviceModel.batch = formRecord.get('batch');
                            slaveDeviceModel.shipmentDate = wizard.down('#deviceAdd #deviceAddShipmentDate').getValue().getTime();
                            slaveDeviceModel.version = formRecord.get('version');

                            me.wizardInformation.slaveMRID = formRecord.get('mRID');
                            me.wizardInformation.slaveShipmentDate = slaveDeviceModel.shipmentDate;
                            me.wizardInformation.slaveDeviceTypeId = formRecord.get('deviceTypeId');
                            me.wizardInformation.slaveDeviceConfigurationId = formRecord.get('deviceConfigurationId');
                            endMethod();
                        } else {
                            Ext.suspendLayouts();
                            step1ErrorMsg.show();
                            mRIDField.markInvalid(Uni.I18n.translate('general.mrid.shouldBeUnique', 'MDC', 'MRID must be unique'));
                            Ext.resumeLayouts(true);
                        }
                    }
                });
            };

        if (!mRIDValid || !deviceTypeValid || !deviceConfigValid) {
            step1ErrorMsg.show();
        } else {
            wizard.down('#deviceAdd form').updateRecord();
            checkMRID();
        }
    },

    prepareStep2: function () {
        var me = this,
            wizard = me.getWizard(),
            loadProfileConfigStore = me.getStore('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration');

        wizard.setLoading();
        loadProfileConfigStore.getProxy().setUrl(me.wizardInformation.slaveDeviceTypeId, me.wizardInformation.slaveDeviceConfigurationId);
        loadProfileConfigStore.load({
            callback: function (loadProfileConfigRecords) {
                var channelRecords = [];
                Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                    if (dataLoggerSlaveDeviceRecord.id === 0) { // the container of the unlinked channels
                        if (dataLoggerSlaveDeviceRecord.dataLoggerSlaveChannelInfos){
                            Ext.Array.forEach(dataLoggerSlaveDeviceRecord.dataLoggerSlaveChannelInfos, function(dataLoggerSlaveChannelInfoRecord){
                                channelRecords.push(dataLoggerSlaveChannelInfoRecord.dataLoggerChannel);
                                if ( !Ext.isEmpty(dataLoggerSlaveChannelInfoRecord.availabilityDate) && dataLoggerSlaveChannelInfoRecord.availabilityDate !== 0) {
                                    if ( Ext.isEmpty(me.wizardInformation.minimalLinkingDate) ||
                                         me.wizardInformation.minimalLinkingDate < dataLoggerSlaveChannelInfoRecord.availabilityDate ) {
                                        me.wizardInformation.minimalLinkingDate = dataLoggerSlaveChannelInfoRecord.availabilityDate;
                                        me.wizardInformation.noData = false;
                                    }
                                }
                            }, me);
                        }
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
                if (!errorsFound) {
                    errorsFound = true;
                    step2ErrorMsg.show();
                }
                channelCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This field is required'));
            } else {
                if (channelCombo.getValue() in channelsMapped) {
                    if (!errorsFound) {
                        errorsFound = true;
                        step2ErrorMsg.show();
                    }
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
        registerConfigStore.getProxy().setExtraParam('deviceType', me.wizardInformation.slaveDeviceTypeId);
        registerConfigStore.getProxy().setExtraParam('deviceConfig', me.wizardInformation.slaveDeviceConfigurationId);
        registerConfigStore.load({
            callback: function (registerConfigRecords) {
                var registerRecords = [];
                Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                    if (dataLoggerSlaveDeviceRecord.id === 0) { // the container of the unlinked registers
                        if (dataLoggerSlaveDeviceRecord.dataLoggerSlaveRegisterInfos){
                            Ext.Array.forEach(dataLoggerSlaveDeviceRecord.dataLoggerSlaveRegisterInfos, function(dataLoggerSlaveRegisterInfoRecord){
                                registerRecords.push(dataLoggerSlaveRegisterInfoRecord.dataLoggerRegister);
                                if ( !Ext.isEmpty(dataLoggerSlaveRegisterInfoRecord.availabilityDate) && dataLoggerSlaveRegisterInfoRecord.availabilityDate !== 0) {
                                    if ( Ext.isEmpty(me.wizardInformation.minimalLinkingDate) ||
                                         me.wizardInformation.minimalLinkingDate < dataLoggerSlaveRegisterInfoRecord.availabilityDate ) {
                                        me.wizardInformation.minimalLinkingDate = dataLoggerSlaveRegisterInfoRecord.availabilityDate;
                                        me.wizardInformation.noData = true;
                                    }
                                }
                            }, me);
                        }
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
                if (!errorsFound) {
                    errorsFound = true;
                    step3ErrorMsg.show();
                }
                registerCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This field is required'));
            } else {
                if (registerCombo.getValue() in registersMapped) {
                    if (!errorsFound) {
                        errorsFound = true;
                        step3ErrorMsg.show();
                    }
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
        // a. Determine the minimal linking date
        var me = this,
            wizard = me.getWizard(),
            minimalLinkingDateMidnight = 0,
            linkingDateToSuggest = new Date();

        if ( !Ext.isEmpty(me.wizardInformation.minimalLinkingDate) && me.wizardInformation.minimalLinkingDate!==0 ) {
            // To avoid the disabling of certain hours, we pass midnight:
            var momentOfDate = moment(me.wizardInformation.minimalLinkingDate);
            momentOfDate.startOf('day');
            minimalLinkingDateMidnight = momentOfDate.unix() * 1000;
        }

        // b. Determine the linking date to suggest
        if (me.wizardInformation) {
            if (me.wizardInformation.linkingDate) {
                linkingDateToSuggest = me.wizardInformation.linkingDate;
            } else if (me.wizardInformation.noData) {
                if (me.wizardInformation.useExisting) {
                    linkingDateToSuggest = me.wizardInformation.slaveShipmentDate;
                } else {
                    linkingDateToSuggest = new Date();
                }
            } else {
                linkingDateToSuggest = minimalLinkingDateMidnight;
            }
        }
        wizard.down('dataloggerslave-link-wizard-step4').initialize(minimalLinkingDateMidnight, linkingDateToSuggest);
    },

    validateStep4: function(callback) {
        var me = this,
            dateField = me.getWizard().down('#mdc-step4-linking-date'),
            errorMsg,
            endMethod = function() {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        me.wizardInformation.linkingDate = dateField.getValue();

        if (me.wizardInformation.linkingDate < me.wizardInformation.minimalLinkingDate) {
            me.getStep4FormErrorMessage().show();

            if (me.wizardInformation.noData) {
                errorMsg = Uni.I18n.translate('general.linkingDateShouldLieAfterX', 'MDC',
                    'The linking date should be equal to or lie after the shipment date of the data logger ({0})',
                    Uni.DateTime.formatDateTime(new Date(me.wizardInformation.minimalLinkingDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT)
                );
            } else {
                errorMsg = Uni.I18n.translate('general.linkingDateShouldLieAfterX', 'MDC',
                    'The linking date should be equal to or lie after the date of the most recent unlinked reading of the channels and registers of the data logger ({0})',
                    Uni.DateTime.formatDateTime(new Date(me.wizardInformation.minimalLinkingDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT)
                );
            }
            me.getWizard().down('#mdc-step4-linking-date').markInvalid(errorMsg);

        } else {
            Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                dataLoggerSlaveDeviceRecord["linkingTimeStamp"] = me.wizardInformation.linkingDate.getTime()/1000;
            }, me);
            endMethod();
        }
    },

    prepareStep5: function() {
        var me = this;
        me.getWizard().down('dataloggerslave-link-wizard-step5').initialize(me.wizardInformation.dataLogger.get('mRID'), me.getSlaveMRID());
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'unlinkSlave':
                Ext.widget('dataloggerslave-unlink-window', {
                    title: Uni.I18n.translate('general.unlinkX', 'MDC', "Unlink '{0}'?", menu.record.get('mRID')),
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
                    Uni.I18n.translate('general.slaveXLinkedToDataLoggerY.success', 'MDC', "Slave '{0}' has been linked to data logger '{1}'."),
                    me.getSlaveMRID(),
                    me.wizardInformation.dataLogger.get('mRID')
                );
                Ext.suspendLayouts();
                infoMessagePanel.update(message);
                finishBtn.show();
                progressbar.hide();
                Ext.resumeLayouts(true);
            },
            failure: function (record, operation) {
                message = Ext.String.format(
                    Uni.I18n.translate('general.slaveXLinkedToDataLoggerY.noSuccess', 'MDC',
                        "Slave '{0}' has not been linked to data logger '{1}' due to a failure. Please try again.",
                        me.getSlaveMRID(),
                        me.wizardInformation.dataLogger.get('mRID')
                    )
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

    doUnlinkTheSlave: function() {
        var me = this;

        me.wizardInformation.dataLogger.save({
            success: function (record) {
                // Nothing to do here
            },
            failure: function (record, operation) {
                // According to the specs this can't occur
            }
        });
    },

    getSlaveMRID: function() {
        var me = this,
            wizard = me.getWizard(),
            step1RadioGroup = wizard.down('#mdc-step1-radiogroup'),
            slaveCombo = wizard.down('#mdc-step1-slave-combo');

        if (step1RadioGroup.getValue().useExisting) {
            return slaveCombo.getValue();
        } else {
            return me.getMRIDField().getValue();
        }
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
                !Ext.isEmpty(me.wizardInformation.minimalLinkingDate);

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
        this.wizardInformation.minimalLinkingDate = 0;
    },

    onUnlinkDataLoggerSlave: function() {
        var me = this,
            unlinkWindow = me.getUnlinkWindow(),
            unlinkDate = unlinkWindow.down('#mdc-dataloggerslave-unlink-window-date-picker').getValue(),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            doUnlink = function() {
                Ext.Array.forEach(me.wizardInformation.dataLogger.get('dataLoggerSlaveDevices'), function(dataLoggerSlaveDeviceRecord){
                    if (dataLoggerSlaveDeviceRecord.id !== 0) { // the container of the unlinked channels
                        dataLoggerSlaveDeviceRecord['unlinkingTimeStamp'] = unlinkDate.getTime()/1000;
                    }
                }, me);

                me.doUnlinkTheSlave();

                // Update grid:
                me.getSlavesGrid().getStore().removeAt(
                    me.getSlavesGrid().getStore().findBy(function(record) {
                        return record.get('mRID') === unlinkWindow.dataLoggerSlaveRecord.get('mRID');
                    })
                );
                mainView.setLoading(false);
            };

        unlinkWindow.close();
        mainView.setLoading();
        setTimeout(doUnlink, 1500);
    }

});

