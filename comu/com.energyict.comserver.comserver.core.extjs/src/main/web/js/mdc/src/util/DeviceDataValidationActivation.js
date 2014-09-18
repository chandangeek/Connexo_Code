Ext.define('Mdc.util.DeviceDataValidationActivation', {
    requires: [
        'Mdc.view.setup.device.DeviceDataValidationPanel',
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView',
        'Mdc.model.DeviceValidation'
    ],

    mRID: null,
    dataValidationLastChecked: null,

    updateDataValidationStatusSection: function (mRID, view) {
        var me = this;
        me.mRID = mRID;
        if (view.down('#deviceDataValidationStatusField')) {
            view.down('#deviceDataValidationStatusField').setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
            view.down('#deviceDataValidationStateChangeBtn').setText(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
            view.down('#deviceDataValidationStateChangeBtn').setDisabled(true);
        }
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + mRID + '/validationrulesets/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (view.down('#dataValidationStatusPanel')) {
                    view.down('#deviceDataValidationStatusField').setValue(res.isActive ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inctive')
                    );
                    view.down('#deviceDataValidationStateChangeBtn').setText((res.isActive ?
                        Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                        Uni.I18n.translate('general.activate', 'MDC', 'Activate')) +
                        ' ' + Uni.I18n.translate('device.dataValidation.statusSection.buttonAppendix', 'MDC', 'data validation')
                    );
                    view.down('#deviceDataValidationStateChangeBtn').action = res.isActive ? 'deactivate' : 'activate';
                    view.down('#deviceDataValidationStateChangeBtn').setDisabled(false);
                } else {
                    var record = Ext.create('Mdc.model.DeviceValidation', res);
                    view.down('#deviceDataValidationForm').loadRecord(record);
                    if (res.isActive) {
                        view.down('#activate').hide();
                        view.down('#deactivate').show();
                    } else {
                        view.down('#deactivate').hide();
                        view.down('#activate').show();
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
                confirmation: function () {
                    me.activateDataValidation(view, this);
                }
            });
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.hasValidation = res.hasValidation;
                me.dataValidationLastChecked = res.lastChecked;
                if (res.hasValidation) {
                    confirmationWindow.add(me.getActivationConfirmationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translatePlural('device.dataValidation.activateConfirmation.title', me.mRID, 'MDC', 'Activate data validation on device {0}?'),
                        msg: ''
                    });
                } else {
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

    activateDataValidation: function (view, confWindow) {
        var me = this;
        if (me.hasValidation) {
            var isValidationRunImmediately = confWindow.down('#validationRunRg').getValue().validationRun === 'now';
            var isWaitForNewData = confWindow.down('#validationRunRg').getValue().validationRun === 'waitForNewData';
        }
        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'true',
                lastChecked: (me.hasValidation ? confWindow.down('#validationFromDate').getValue().getTime() : null)
            },
            success: function () {
                me.updateDataValidationStatusSection(me.mRID, view);
                if (isValidationRunImmediately) {
                    me.isValidationRunImmediately = true;
                    me.validateData(confWindow);
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation on device {0} was activated successfully'));
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
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            success: function () {
                me.destroyConfirmationWindow();
                if (me.isValidationRunImmediately) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation on device {0} was completed successfully'));
                } else {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation on device {0} was activated successfully'));
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
                    value: new Date(me.dataValidationLastChecked),
                    fieldLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item1', 'MDC', '1. Validate data from'),
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
                    xtype: 'displayfield',
                    value: '',
                    padding: '0 0 -10 0',
                    fieldLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2', 'MDC', '2. When do you want to run the data validation?'),
                    labelWidth: 350
                },
                {
                    xtype: 'radiogroup',
                    itemId: 'validationRunRg',
                    columns: 1,
                    defaults: {
                        name: 'validationRun',
                        padding: '-10 0 0 60'
                    },
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2.1', 'MDC', 'Run now'),
                            inputValue: 'now'
                        },
                        {
                            boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2.2', 'MDC', 'Wait for new data'),
                            inputValue: 'waitForNewData',
                            checked: true
                        }
                    ]
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
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'false'
            },
            success: function () {
                me.updateDataValidationStatusSection(me.mRID, view);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.deactivation.successMsg', me.mRID, 'MDC', 'Data validation on device {0} was deactivated successfully'));
            }
        });
    },

    destroyConfirmationWindow: function () {
        if (Ext.ComponentQuery.query('#activationConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].removeAll(true);
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].destroy();
        }
    },

    confirmationWindowButtonsDisable: function (value) {
        if (Ext.ComponentQuery.query('#activationConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('button[name=confirm]').setDisabled(value);
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('button[name=cancel]').setDisabled(value);
        }
    },

    showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#activationConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors').update(errors);
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors').setVisible(true);
        }
    },

    onValidationFromDateChange: function () {
        if (Ext.ComponentQuery.query('#activationConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors').update('');
            Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors').setVisible(false);
        }
    }
});

