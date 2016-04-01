Ext.define('Mdc.controller.setup.DataLoggerSlaves', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.DataLoggerSlaves',
        'Mdc.store.AvailableDataLoggerSlaves',
        'Mdc.store.Devices'
    ],

    views: [
        'Mdc.view.setup.dataloggerslaves.Setup',
        'Mdc.view.setup.dataloggerslaves.LinkContainer',
        'Mdc.view.setup.device.DeviceAdd'
    ],

    refs: [
        {ref: 'wizard', selector: '#mdc-dataloggerslave-link-wizard'},
        {ref: 'navigationMenu', selector: '#mdc-link-dataloggerslave-navigation-menu'},
        {ref: 'step1Panel', selector: '#mdc-dataloggerslave-link-wizard-step1'},
        {ref: 'step2Panel', selector: '#mdc-dataloggerslave-link-wizard-step2'},
        {ref: 'step1FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step1-errors'},
        {ref: 'mRIDField', selector: '#deviceAddMRID'},
        {ref: 'deviceTypeCombo', selector: '#deviceAddType'},
        {ref: 'deviceConfigCombo', selector: '#deviceAddConfig'},
        {ref: 'step2FormErrorMessage', selector: '#mdc-dataloggerslave-link-wizard-step2-errors'}
    ],

    init: function () {
        this.control({
            '#mdc-link-dataloggerslave-btn': {
                click: this.onLinkDataLoggerSlave
            },
            'dataloggerslave-link-wizard-step1 #mdc-step1-radiogroup': {
                change: this.onStep1OptionChange
            },
            '#mdc-dataloggerslave-link-wizard button[navigationBtn=true]': {
                click: this.moveTo
            },
            '#mdc-dataloggerslave-link-wizard #mdc-link-dataloggerslave-navigation-menu': {
                movetostep: this.moveTo
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
            dataLoggerMRID = router.arguments.mRID,
            widget = Ext.widget('dataloggerslave-link-container', {
                itemId: 'mdc-dataloggerslave-link-container',
                router: router,
                service: me.service,
                returnLink: router.getRoute('devices/device/dataloggerslaves').buildUrl()
            });

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
                    doCallback();
                    me.prepareStep2();
                });
                break;
            case 2:
                if (me.validateStep2()) {
                    doCallback();
                }
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
                navigationMenu.jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.hide();
                me.prepareStep3(wizard, navigationMenu, [confirmBtn, backBtn, cancelBtn]);
                break;
            case 4:
                navigationMenu.jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.hide();
                me.prepareStep4(wizard, finishBtn, navigationMenu);
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
            var slaveCombo = me.getStep1Panel().down('#mdc-step1-slave-combo');
            slaveCombo.allowBlank = false;
            if (!slaveCombo.validate()) {
                step1ErrorMsg.show();
            } else {
                slaveCombo.allowBlank = true;
                endMethod();
            }
            return;
        }

        var mRIDField = me.getMRIDField(),
            mRIDValid = mRIDField.validate(),
            deviceTypeCombo = me.getDeviceTypeCombo(),
            deviceTypeValid = deviceTypeCombo.validate(),
            deviceConfigCombo = me.getDeviceConfigCombo(),
            deviceConfigValid = deviceConfigCombo.validate(),
            mRID = mRIDField.getValue(),
            formRecord = wizard.down('#deviceAdd form').getRecord();

        if (!mRIDValid || !deviceTypeValid || !deviceConfigValid) {
            step1ErrorMsg.show();
        } else if (mRID !== formRecord.get('mRID')) {
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
                        step1ErrorMsg.hide();
                        callback();
                    } else {
                        Ext.suspendLayouts();
                        step1ErrorMsg.show();
                        mRIDField.markInvalid(Uni.I18n.translate('general.mrid.shouldBeUnique', 'MDC', 'MRID must be unique'));
                        Ext.resumeLayouts(true);
                    }
                }
            });
        } else {
            endMethod();
        }
    },

    prepareStep2: function () {

    }

});

