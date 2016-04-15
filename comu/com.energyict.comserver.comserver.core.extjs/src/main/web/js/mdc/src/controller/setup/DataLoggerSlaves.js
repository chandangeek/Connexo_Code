Ext.define('Mdc.controller.setup.DataLoggerSlaves', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.DataLoggerSlaves',
        'Mdc.store.AvailableDataLoggerSlaves',
        'Mdc.store.Devices',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.store.RegisterConfigsOfDeviceConfig'
    ],

    views: [
        'Mdc.view.setup.dataloggerslaves.Setup',
        'Mdc.view.setup.dataloggerslaves.LinkContainer',
        'Mdc.view.setup.device.DeviceAdd'
    ],

    wizardInformation: null,

    refs: [
        {ref: 'wizard', selector: '#mdc-dataloggerslave-link-wizard'},
        {ref: 'navigationMenu', selector: '#mdc-link-dataloggerslave-navigation-menu'},
        {ref: 'step1Panel', selector: '#mdc-dataloggerslave-link-wizard-step1'},
        {ref: 'step2Panel', selector: '#mdc-dataloggerslave-link-wizard-step2'},
        {ref: 'step1FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step1-errors'},
        {ref: 'mRIDField', selector: '#deviceAddMRID'},
        {ref: 'deviceTypeCombo', selector: '#deviceAddType'},
        {ref: 'deviceConfigCombo', selector: '#deviceAddConfig'},
        {ref: 'step2FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step2-errors'},
        {ref: 'step3FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step3-errors'},
        {ref: 'step4FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step4-errors'}
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

    showLinkWizard: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('dataloggerslave-link-container', {
                itemId: 'mdc-dataloggerslave-link-container',
                router: router,
                service: me.service,
                returnLink: router.getRoute('devices/device/dataloggerslaves').buildUrl()
            });

        me.wizardInformation = {};
        me.wizardInformation.dataLoggerMRID = router.arguments.mRID;
        me.getApplication().fireEvent('changecontentevent', widget);
        mainView.setLoading(true);
        widget.down('dataloggerslave-link-wizard').loadRecord(Ext.create('Mdc.model.Device'));
        widget.down('#mdc-step1-slave-combo').getStore().load(function() {
            mainView.setLoading(false);
        });
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
                addDevicePanel = Ext.widget('deviceAdd', { labelWidth: 145, formWidth: 545 });
                addDevicePanel.down('panel').title = '';
                addDevicePanel.down('#deviceAddArrival').hide();
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
                me.wizardInformation.slaveMRID = slaveDevice.get('mRID');
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
                            me.wizardInformation.slaveMRID = formRecord.get('mRID');
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
            loadProfileConfigStore = me.getStore('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration'),
            channelsStore = me.getStore('Mdc.store.ChannelsOfLoadProfilesOfDevice');

        wizard.setLoading();
        loadProfileConfigStore.getProxy().setUrl(me.wizardInformation.slaveDeviceTypeId, me.wizardInformation.slaveDeviceConfigurationId);
        loadProfileConfigStore.load({
            callback: function (loadProfileConfigRecords) {
                channelsStore.getProxy().setUrl(me.wizardInformation.dataLoggerMRID);
                channelsStore.load({
                    callback: function (channelRecords) {
                        wizard.setLoading(false);
                        wizard.down('dataloggerslave-link-wizard-step2').initialize(
                            loadProfileConfigRecords,
                            channelRecords,
                            me.wizardInformation ? me.wizardInformation.mappedChannels : undefined
                        );
                    }
                });
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
                channelCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This is a required field'));
            } else {
                if (channelCombo.getValue() in channelsMapped) {
                    if (!errorsFound) {
                        errorsFound = true;
                        step2ErrorMsg.show();
                    }
                    channelCombo.markInvalid(Uni.I18n.translate('general.channelAlreadyMapped', 'MDC', 'This channel is already mapped'));
                } else {
                    channelsMapped[channelCombo.getValue()] = true;
                    mappedChannels[counter] = channelCombo.getValue();
                }
            }
        }

        if (!errorsFound) {
            me.wizardInformation.mappedChannels = mappedChannels;
            endMethod();
        }
    },

    prepareStep3: function() {
        var me = this,
            wizard = me.getWizard(),
            registerConfigStore = me.getStore('Mdc.store.RegisterConfigsOfDeviceConfig'),
            dataLoggerRegistersStore = me.getStore('Mdc.store.RegisterConfigsOfDevice');

        wizard.setLoading();
        registerConfigStore.getProxy().setExtraParam('deviceType', me.wizardInformation.slaveDeviceTypeId);
        registerConfigStore.getProxy().setExtraParam('deviceConfig', me.wizardInformation.slaveDeviceConfigurationId);
        registerConfigStore.load({
            callback: function (registerConfigRecords) {
                dataLoggerRegistersStore.getProxy().setExtraParam('mRID', me.wizardInformation.dataLoggerMRID);
                dataLoggerRegistersStore.load({
                    callback: function (dataLoggerRegisterRecords) {
                        wizard.setLoading(false);
                        wizard.down('dataloggerslave-link-wizard-step3').initialize(
                            registerConfigRecords,
                            dataLoggerRegisterRecords,
                            me.wizardInformation ? me.wizardInformation.mappedRegisters : undefined
                        );
                    }
                });
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
                registerCombo.markInvalid(Uni.I18n.translate('general.requiredField', 'MDC', 'This is a required field'));
            } else {
                if (registerCombo.getValue() in registersMapped) {
                    if (!errorsFound) {
                        errorsFound = true;
                        step3ErrorMsg.show();
                    }
                    registerCombo.markInvalid(Uni.I18n.translate('general.registerAlreadyMapped', 'MDC', 'This register is already mapped'));
                } else {
                    registersMapped[registerCombo.getValue()] = true;
                    mappedRegisters[counter] = registerCombo.getValue();
                }
            }
        }

        if (!errorsFound) {
            me.wizardInformation.mappedRegisters = mappedRegisters;
            endMethod();
        }
    },

    prepareStep4: function() {
        // Determine the arrival date to suggest
        var me = this,
            wizard = me.getWizard();

        if (me.wizardInformation && me.wizardInformation.arrivalDate) {
            wizard.down('dataloggerslave-link-wizard-step4').initialize(me.wizardInformation.arrivalDate);
        } else {
            wizard.down('dataloggerslave-link-wizard-step4').initialize(new Date());
        }
    },

    validateStep4: function(callback) {
        var me = this,
            dateField = me.getWizard().down('#mdc-step4-arrival-date'),
            endMethod = function() {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        // TODO: Validate the given arrival date

        me.wizardInformation.arrivalDate = dateField.getValue();
        endMethod();
    },

    prepareStep5: function() {
        var me = this;
        me.getWizard().down('dataloggerslave-link-wizard-step5').initialize(me.wizardInformation.dataLoggerMRID, me.getSlaveMRID());
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'unlinkSlave':
                console.log('To do: unlink the device');
                break;
        }
    },

    linkTheSlave: function(finishBtn) {
        var me = this,
            wizard = me.getWizard(),
            progressbar = wizard.down('#mdc-dataloggerslave-link-wizard-step6-progressbar'),
            doLink = function() {
                Ext.suspendLayouts();
                wizard.down('dataloggerslave-link-wizard-step6').update(
                    Ext.String.format(
                        Uni.I18n.translate('general.slaveXLinkedToDataLoggerY', 'MDC', "Slave '{0}' has been linked to data logger '{1}'."),
                        me.getSlaveMRID(),
                        me.wizardInformation.dataLoggerMRID
                    )
                );
                finishBtn.show();
                progressbar.hide();
                Ext.resumeLayouts(true);
            };

        Ext.suspendLayouts();
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        wizard.down('dataloggerslave-link-wizard-step6').update(
            Uni.I18n.translate('linkSlave.wizard.waitMsg', 'MDC', 'Busy linking the slave. Please wait...')
        );
        Ext.resumeLayouts(true);

        setTimeout(doLink, 1500);
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
                !Ext.isEmpty(me.wizardInformation.arrivalDate);

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
        this.wizardInformation.arrivalDate = undefined;
    }
});

