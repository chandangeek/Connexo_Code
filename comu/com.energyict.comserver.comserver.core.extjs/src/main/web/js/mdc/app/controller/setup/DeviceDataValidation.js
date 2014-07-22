Ext.define('Mdc.controller.setup.DeviceDataValidation', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    stores: [
        'DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    views: [
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView'
    ],
    refs: [
        {ref: 'rulesSetGrid', selector: '#deviceDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataValidationRulesSetPreviewCt'},
        {ref: 'rulesGrid', selector: '#deviceDataValidationRulesGrid'},
        {ref: 'rulePreview', selector: '#deviceDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'},
        {ref: 'deviceDataValidationStateChangeButton', selector: '#deviceDataValidationStateChangeButton'},
        {ref: 'activationConfirmationWindow', selector: '#activationConfirmationWindow'},
        {ref: 'validationFromDate', selector: '#validationFromDate'},
        {ref: 'validationDateErrors', selector: '#validationDateErrors'},
        {ref: 'validationRunRg', selector: '#validationRunRg'},
        {ref: 'validationProgress', selector: '#validationProgress'}
    ],
    mRID: null,
    activatedMRID: null,
    dataValidationIsActive: null,
    dataValidationLastChecked: null,
    init: function () {
        this.control({
            '#deviceDataValidationRulesSetGrid': {
                itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
            },
            '#deviceDataValidationRulesGrid': {
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetState
            },
            '#deviceDataValidationStateChangeButton': {
                click: this.changeDeviceActivationStatusState
            },
            '#validationFromDate': {
                change: this.onValidationFromDateChange
            }
        });
        this.callParent();
    },
    showDeviceDataValidationMainView: function (mRID) {
        var me = this;
        me.mRID = mRID;
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
        Ext.Ajax.request({
            url: '/apps/mdc/app/store/DeviceStatusActivationFake.json',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText),
                    widget = Ext.widget('deviceDataValidationRulesSetMainView', {
                        mRID: mRID,
                        dataValidationIsActive: res.isActive
                    });
                me.dataValidationIsActive = res.isActive;
                me.dataValidationLastChecked = res.lastChecked;
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },
    onRulesSetGridSelectionChange: function (grid) {
        this.getRulesSetPreviewCt().removeAll(true);
        var rulesSetPreview = Ext.widget('deviceDataValidationRulesSetPreview', {
            rulesSetId: grid.lastSelected.get('id'),
            title: grid.lastSelected.get('name')
        });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
    },
    onRulesGridSelectionChange: function (grid) {
        this.getRulePreview().updateValidationRule(grid.lastSelected);
    },
    onRulesSetGridItemClick: function (gridView, record, el, idx, e) {
        var target = e.getTarget(null, null, true);
        if (target.hasCls('x-action-col-icon')) {
            var menuItem = this.getChangeRuleSetStateActionMenuItem();
            menuItem.setText(record.get('isActive') ?
                Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'MDC', 'Activate'))
        }
    },
    changeRuleSetState: function () {
        var record = this.getRulesSetGrid().getSelectionModel().getLastSelected();
        record.set('isActive', !record.get('isActive'));
    },
    changeDeviceActivationStatusState: function () {
        this.dataValidationIsActive ? this.showDeactivationConfirmation() : this.showActivationConfirmation();
    },
    showDeactivationConfirmation: function () {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate')
        }).show({
                title: Ext.String.format(Uni.I18n.translate('device.dataValidation.deactivateConfirmation.title', 'MDC', 'Deactivate data validation on device {0}?'), me.mRID),
                msg: Uni.I18n.translate('device.dataValidation.deactivateConfirmation.msg', 'MDC', 'The data of this device will no longer be validated'),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.deactivateDataValidation();
                    }
                }
            });
    },
    deactivateDataValidation: function () {
        var me = this;
        Ext.Ajax.request({
            url: 'api/ddr/devices/' + me.mRID + '/validation/deactivate',
            method: 'PUT',
            success: function () {
                me.showDeviceDataValidationMainView(me.mRID);
                me.getApplication().fireEvent('acknowledge',
                    Ext.String.format(Uni.I18n.translate('device.dataValidation.deactivation.successMsg', 'MDC', 'Data validation on device {0} was deactivated successfully'), me.mRID));
            }
        });
    },
    showActivationConfirmation: function () {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'activationConfirmationWindow',
                confirmText: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                confirmation: function () {
                    me.activateDataValidation();
                }
            });
        confirmationWindow.add(this.getActivationConfirmationContent());
        confirmationWindow.show({
            title: Ext.String.format(Uni.I18n.translate('device.dataValidation.activateConfirmation.title', 'MDC', 'Activate data validation on device {0}?'), me.mRID),
            msg: ''
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },
    activateDataValidation: function () {
        var me = this,
            isValidationRunImmediately = this.getValidationRunRg().getValue().validationRun === 'now';
        me.activatedMRID = me.mRID;
        me.getActivationConfirmationWindow().down('button[name=confirm]').setDisabled(true);
        if (isValidationRunImmediately) {
            me.getValidationProgress().add(Ext.create('Ext.ProgressBar', {
                    margin: '5 0 15 0'
                })).wait({
                    duration: 5000,
                    text: Uni.I18n.translate('device.dataValidation.isInProgress', 'MDC', 'Data validation is in progress. Please wait...'),
                    fn: function () {
                        me.getActivationConfirmationWindow().removeAll(true);
                        me.getActivationConfirmationWindow().close();
                        me.showDeviceDataValidationMainView(me.mRID);
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
        }
        Ext.Ajax.request({
            url: 'api/ddr/devices/' + me.mRID + '/validation/activate',
            method: 'PUT',
            timeout: 600000,
            jsonData: {
                validateFrom: me.getValidationFromDate().getValue(),
                run: me.getValidationRunRg().getValue().validationRun
            },
            success: function () {
                if (me.getActivationConfirmationWindow()) {
                    me.getActivationConfirmationWindow().removeAll(true);
                    me.getActivationConfirmationWindow().destroy();
                }
                if (me.isUserAtTheSameLocation()) me.showDeviceDataValidationMainView(me.mRID);
                me.getApplication().fireEvent('acknowledge', isValidationRunImmediately ?
                    Ext.String.format(Uni.I18n.translate('device.dataValidation.activation.validated', 'MDC', 'Data validation on device {0} was completed successfully'), me.mRID) :
                    Ext.String.format(Uni.I18n.translate('device.dataValidation.activation.activated', 'MDC', 'Data validation on device {0} was activated successfully'), me.mRID));
            },
            failure: function () {
                if (me.getActivationConfirmationWindow()) {
//                    me.getValidationProgress().removeAll(true);
                    me.showValidationActivationErrors('The date should be before or equal to the default date'); // Will be in the backend response
                    me.getActivationConfirmationWindow().down('button[name=confirm]').setDisabled(false);
                }
            }
        });
    },
    showValidationActivationErrors: function (errors) {
        this.getValidationDateErrors().update(errors);
        this.getValidationDateErrors().setVisible(true);
    },
    onValidationFromDateChange: function () {
        this.getValidationDateErrors().update('');
        this.getValidationDateErrors().setVisible(false);
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
    isUserAtTheSameLocation: function () {
        var splittedUrl = document.URL.split('/'),
            isDataValidationRoot = splittedUrl[splittedUrl.length - 1] === 'datavalidation',
            currentMRID = splittedUrl[splittedUrl.length - 2];
        return isDataValidationRoot && this.activatedMRID === currentMRID;
    }
});