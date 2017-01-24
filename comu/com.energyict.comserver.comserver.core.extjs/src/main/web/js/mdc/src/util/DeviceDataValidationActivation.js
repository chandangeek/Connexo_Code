Ext.define('Mdc.util.DeviceDataValidationActivation', {
    requires: [
        'Mdc.view.setup.device.DeviceDataValidationPanel',
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView',
        'Mdc.model.DeviceValidation'
    ],

    deviceId: null,
    dataValidationLastChecked: null,
	validationOnStorage: null,

    updateDataValidationStatusSection: function (deviceId, view, deviceRecord) {
        var me = this;
        me.deviceId = deviceId;
        if (view.down('#deviceDataValidationStatusField')) {
            view.down('#deviceDataValidationStatusField').setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
            view.down('#dataValidationStatusPanel').setLoading(true);
            !!view.down('#deviceDataValidationStateChangeBtn') && view.down('#deviceDataValidationStateChangeBtn').setDisabled(true);
        }
        if (view && view.device && Ext.isEmpty(deviceRecord)) {
            me.getModel('Mdc.model.Device').load(deviceId, {
                success: function (record) {
                    view.device = record;
                }
            });
        } else if(view && view.device) {
            view.device = deviceRecord;
        }

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/validationrulesets/validationstatus',
            method: 'GET',
            timeout: 60000,
            callback: function () {
                var dataValidationStatusPanel = view.down('#dataValidationStatusPanel');

                if (dataValidationStatusPanel) {
                    dataValidationStatusPanel.setLoading(false);
                }
            },
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (view.down('#dataValidationStatusPanel')) {
                    view.down('#deviceDataValidationStatusField').setValue(res.isActive ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                    );
                    if (!!view.down('#deviceDataValidationStateChangeBtn')) {
                        view.down('#deviceDataValidationStateChangeBtn').setText((res.isActive ?
                                Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                                Uni.I18n.translate('general.activate', 'MDC', 'Activate')) +
                            ' ' + Uni.I18n.translate('device.dataValidation.statusSection.buttonAppendix', 'MDC', 'data validation')
                        );
                        view.down('#deviceDataValidationStateChangeBtn').action = res.isActive ? 'deactivate' : 'activate';
                        view.down('#deviceDataValidationStateChangeBtn').setDisabled(false);
                    }

                } else {
                    var record = Ext.create('Mdc.model.DeviceValidation', res);
                    if (!view.isDestroyed
                    && !!view.down('#deviceDataValidationForm')) {
                        view.down('#deviceDataValidationForm').loadRecord(record);
                        view.down('#fld-validation-result').setValue(record);
                    }
                }
            }
        });
    },

    showActivationConfirmation: function (view) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'activationConfirmationWindow',
                confirmText: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
				confirmAndRunText: Uni.I18n.translate('general.activateAndRun', 'MDC', 'Activate & Run'),
                confirmation: function () {
                    me.activateDataValidation(view, this, false);
                },
				confirmationAndRun: function () {
                    me.activateDataValidation(view, this, true);
                }
				
            });
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validationstatusactivation',
            method: 'GET',
            timeout: 60000,
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.hasValidation = res.hasValidation;
                if (res.lastChecked) {
                    me.dataValidationLastChecked = new Date(res.lastChecked);
                } else {
                    me.dataValidationLastChecked = new Date();
                }
				if (res.isStorage) {
                    me.validationOnStorage = true;
                } else {
                    me.validationOnStorage = false;
                }
				
				
                if (res.hasValidation) {
                    confirmationWindow.insert(1,me.getActivationConfirmationContent());
					
					// remove confirm and cancel buttons
					var buttonConfirm = confirmationWindow.down('button[name=confirm]'),
						buttonCancel = confirmationWindow.down('button[name=cancel]'),
						owner = buttonConfirm.ownerCt;
					
                    owner.remove(buttonConfirm);
                    owner.remove(buttonCancel);		

					// add 3 new buttons		
					owner.insert(1, me.getButtonContent(confirmationWindow));
					
                    confirmationWindow.show({
                        title: Uni.I18n.translate('device.dataValidation.activateConfirmation.title', 'MDC', 'Activate data validation on device {0}?', [me.deviceId]),
                        msg: ''
                    });
                } else {
                    confirmationWindow.insert(1,[{
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.onStorage', 'MDC', 'Validate data on storage'),
                        itemId: 'validationOnStorage',
                        checked: me.validationOnStorage,
                        labelWidth: 175,
                        labelPad: 1,
                        padding: '0 0 15px 48px'
                    }]);
                    confirmationWindow.show({
                        title: Uni.I18n.translate('device.dataValidation.activateConfirmation.title', 'MDC', 'Activate data validation on device {0}?', [me.deviceId]),
                        msg: Uni.I18n.translate('device.dataValidation.activateMsg', 'MDC', 'There are currently no readings for this device')
                    });
                }
            }
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },

    activateDataValidation: function (view, confWindow, runNow) {
        var me = this,
			validationOnStorage, isValidationOnStorage = 'false';
        
		validationOnStorage = confWindow.down('#validationOnStorage');
		if (validationOnStorage){
			isValidationOnStorage = confWindow.down('#validationOnStorage').getValue();
		}
		
        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validationstatus',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: 'true',
				isStorage: isValidationOnStorage,
                lastChecked: (me.hasValidation ? confWindow.down('#validationFromDate').getValue().getTime() : new Date().getTime()),
                device: _.pick(view.device.getRecordData(), 'name', 'version', 'parent')
            },
            success: function () {
                me.updateDataValidationStatusSection(me.deviceId, view);
                if (runNow) {
                    me.isValidationRunImmediately = true;
                    me.getModel('Mdc.model.Device').load(me.deviceId, {
                        success: function (record) {
                            me.validateData(confWindow, record);
                        }
                    });
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('device.dataValidation.activation.activated', 'MDC', 'Data validation activated'));
                }
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);

                if (response.status === 400) {
                    me.showValidationActivationErrors(res.errors[0].msg);
                    me.confirmationWindowButtonsDisable(false);
                } else {
                    me.destroyConfirmationWindow();
                }
            }
        });
    },

    validateData: function (confWindow, device) {
        var me = this;

        confWindow.down('#validationProgress').add(Ext.create('Ext.ProgressBar', {
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

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            isNotEdit: true,
            jsonData: _.pick(device.getRecordData(), 'name', 'version', 'parent'),
            success: function () {
                var view = me.getPage();
                if (view && view.device) {
                    me.getModel('Mdc.model.Device').load(me.deviceId, {
                        success: function (record) {
                            view.device = record;
                        }
                    });
                }
                me.destroyConfirmationWindow();
                if (me.isValidationRunImmediately) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('device.dataValidation.activation.validated', 'MDC', 'Data validation completed'));
                } else {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('device.dataValidation.activation.activated', 'MDC', 'Data validation activated'));
                }
            },
            failure: function (response) {
                var res;

                if (confWindow) {
                    if (response.status === 400) {
                        res= Ext.JSON.decode(response.responseText);
                        confWindow.down('#validationProgress').removeAll(true);
                        me.showValidationActivationErrors(res.errors[0].msg);
                        me.confirmationWindowButtonsDisable(false);
                    } else {
                        me.destroyConfirmationWindow();
                    }
                }
            }
        });
    },

	getButtonContent: function (confWindow) {
        var me = this;
        return Ext.create('Ext.container.Container', {
			layout: {
                    type: 'hbox'
            },
			items: [
				{
					xtype: 'button',
					action: 'confirm',
					name: 'confirm',
					scope: confWindow,
					text: confWindow.confirmText,
					ui: confWindow.confirmBtnUi,
					handler: confWindow.confirmation,
					margin: '0 0 0 ' + confWindow.iconWidth
				},
				{
					xtype: 'button',
					action: 'confirmAndRun',
					name: 'confirmAndRun',
					scope: confWindow,
					text: confWindow.confirmAndRunText,
					ui: confWindow.confirmBtnUi,
					handler: confWindow.confirmationAndRun,		
					margin: '0 0 0 10'				
				},
				{
					xtype: 'button',
					action: 'cancel',
					name: 'cancel',
					scope: confWindow,
					text: confWindow.cancelText,
					ui: 'link',
					handler: confWindow.cancellation
				}				
            ]


           
        });
    },
	
    getActivationConfirmationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left',
                labelStyle: 'font-weight: normal; padding-left: 50px'
            },
            width: 500,
            items: [
                {
                    xtype: 'datefield',
                    itemId: 'validationFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item', 'MDC', 'Validate data from'),
                    labelWidth: 175,
                    labelPad: 1
                },
                {
                    xtype: 'panel',
                    itemId: 'validationDateErrors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
				{
					xtype: 'checkboxfield',
					boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.onStorage', 'MDC', 'Validate data on storage'),
					itemId: 'validationOnStorage',
					checked: me.validationOnStorage,
					labelWidth: 175,
                    labelPad: 1,
					padding: '0 0 15px 48px'
				},
                {
                    xtype: 'panel',
                    itemId: 'validationProgress',
                    layout: 'fit',
                    padding: '0 0 0 50'
                }
            ]
        });
    },

    showDeactivationConfirmation: function (view) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate')
        }).show({
            title: Uni.I18n.translate('device.dataValidation.deactivateConfirmation.title', 'MDC', 'Deactivate data validation on device {0}?', [me.deviceId]),
            msg: Uni.I18n.translate('device.dataValidation.deactivateConfirmation.msg', 'MDC', 'The data of this device will no longer be validated'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.deactivateDataValidation(view);
                }
            }
        });
    },

    deactivateDataValidation: function (view) {
        var me = this;
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/validationstatus',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: 'false',
                device: _.pick(view.device.getRecordData(), 'name', 'version', 'parent')
            },
            success: function () {
                me.updateDataValidationStatusSection(me.deviceId, view);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.dataValidation.deactivation.successMsg', 'MDC', 'Data validation deactivated'));
            }
        });
    },

    destroyConfirmationWindow: function () {
		var activationConfirmationWindow;
		
		activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
            activationConfirmationWindow.removeAll(true);
            activationConfirmationWindow.destroy();
        }
    },

    confirmationWindowButtonsDisable: function (value) {
		var activationConfirmationWindow;
		
		activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
			var button = activationConfirmationWindow.down('button[name=confirm]');
			
			button = activationConfirmationWindow.down('button[name=confirm]');
			if (button){
				button.setDisabled(value);
			}
			
			button = activationConfirmationWindow.down('button[name=confirmAndRun]');
			if (button){
				button.setDisabled(value);
			}
			
			button = activationConfirmationWindow.down('button[name=cancel]');
			if (button){
				button.setDisabled(value);
			}
		}       
    },

    showValidationActivationErrors: function (errors) {
		var activationConfirmationWindow, validationDateErrors;
		
		activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
		if (activationConfirmationWindow){
			validationDateErrors = Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors');
			if (validationDateErrors){
				validationDateErrors.update(errors);
				validationDateErrors.setVisible(true);
			}
		}
    },

    onValidationFromDateChange: function () {
		var activationConfirmationWindow, validationDateErrors;
		
		activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
		if (activationConfirmationWindow){
			validationDateErrors = Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors');
			if (validationDateErrors){
				validationDateErrors.update('');
				validationDateErrors.setVisible(false);
			}
		}        
    }
});

