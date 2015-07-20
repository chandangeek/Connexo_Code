Ext.define('Mdc.util.DeviceDataValidationActivation', {
    requires: [
        'Mdc.view.setup.device.DeviceDataValidationPanel',
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView',
        'Mdc.model.DeviceValidation'
    ],

    mRID: null,
    dataValidationLastChecked: null,
	validationOnStorage: null,

    updateDataValidationStatusSection: function (mRID, view) {
        var me = this;
        me.mRID = mRID;
        if (view.down('#deviceDataValidationStatusField')) {
            view.down('#deviceDataValidationStatusField').setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
            view.down('#dataValidationStatusPanel').setLoading(true);
            !!view.down('#deviceDataValidationStateChangeBtn') && view.down('#deviceDataValidationStateChangeBtn').setDisabled(true);
        }
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
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
                    if (!view.isDestroyed) {
                        !!view.down('#deviceDataValidationForm') && view.down('#deviceDataValidationForm').loadRecord(record);
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
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
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
                        title: Uni.I18n.translatePlural('device.dataValidation.activateConfirmation.title', me.mRID, 'MDC', 'Activate data validation on device {0}?'),
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
                        title: Uni.I18n.translatePlural('device.dataValidation.activateConfirmation.title', me.mRID, 'MDC', 'Activate data validation on device {0}?'),
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
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'true',
				isStorage: isValidationOnStorage,
                lastChecked: (me.hasValidation ? confWindow.down('#validationFromDate').getValue().getTime() : new Date().getTime())
            },
            success: function () {
                me.updateDataValidationStatusSection(me.mRID, view);
                if (runNow) {
                    me.isValidationRunImmediately = true;
                    me.validateData(confWindow);
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation activated'));
                }
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

        Ext.Ajax.suspendEvent('requestexception');

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            success: function () {
                me.destroyConfirmationWindow();
                if (me.isValidationRunImmediately) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation completed'));
                } else {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation activated'));
                }
            },
            failure: function (response) {
                if (confWindow) {
                    var res = Ext.JSON.decode(response.responseText);
                    confWindow.down('#validationProgress').removeAll(true);
                    me.showValidationActivationErrors(res.errors[0].msg);
                    me.confirmationWindowButtonsDisable(false);
                }
            },
            callback: function () {
                Ext.Ajax.resumeEvent('requestexception');
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
            title: Uni.I18n.translatePlural('device.dataValidation.deactivateConfirmation.title', me.mRID, 'MDC', 'Deactivate data validation on device {0}?'),
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
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'false'
            },
            success: function () {
                me.updateDataValidationStatusSection(me.mRID, view);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.deactivation.successMsg', me.mRID, 'MDC', 'Data validation deactivated'));
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

