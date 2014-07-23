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
        {ref: 'dvStatusPanel', selector: '#dataValidationStatusPanel'},
        {ref: 'dvStatusField', selector: '#deviceDataValidationStatusField'},
        {ref: 'dvStatusChangeBtn', selector: '#deviceDataValidationStateChangeBtn'},
        {ref: 'rulesSetGrid', selector: '#deviceDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataValidationRulesSetPreviewCt'},
        {ref: 'rulePreview', selector: '#deviceDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'},
        {ref: 'activationConfirmationWindow', selector: '#activationConfirmationWindow'},
        {ref: 'validationFromDate', selector: '#validationFromDate'},
        {ref: 'validationRunRg', selector: '#validationRunRg'},
        {ref: 'validationProgress', selector: '#validationProgress'},
        {ref: 'validationDateErrors', selector: '#validationDateErrors'}
    ],
    mRID: null,
    activatedMRID: null,
    dataValidationLastChecked: null,
    init: function () {
        this.control({
            '#deviceDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
            },
            '#deviceDataValidationRulesGrid': {
                afterrender: this.onRulesGridAfterRender,
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetStatus
            },
            '#deviceDataValidationStateChangeBtn': {
                click: this.changeDataValidationStatus
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
                var widget = Ext.widget('deviceDataValidationRulesSetMainView', { mRID: mRID });
                me.updateDataValidationStatusSection();
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },
    updateDataValidationStatusSection: function () {
        var me = this;
        me.getDvStatusField().setValue(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
        me.getDvStatusChangeBtn().setText(Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'));
        me.getDvStatusChangeBtn().setDisabled(true);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.dataValidationLastChecked = res.lastChecked;
                if (me.getDvStatusPanel()) {
                    me.getDvStatusField().setValue(res.isActive ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inctive')

                    );
                    me.getDvStatusChangeBtn().setText((res.isActive ?
                        Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                        Uni.I18n.translate('general.activate', 'MDC', 'Activate')) +
                        ' ' + Uni.I18n.translate('device.dataValidation.statusSection.buttonAppendix', 'MDC', 'data validation')
                    );
                    me.getDvStatusChangeBtn().action = res.isActive ? 'deactivate' : 'activate';
                    me.getDvStatusChangeBtn().setDisabled(false);
                }
            }
        });
    },
    changeDataValidationStatus: function (btn) {
        btn.action === 'activate' ? this.showActivationConfirmation() : this.showDeactivationConfirmation();
    },
    onRulesSetGridAfterRender: function (grid) {
        var me = this;
        grid.store.getProxy().setExtraParam('mRID', me.mRID);
        grid.store.load({
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesSetGridSelectionChange: function (grid) {
        this.getRulesSetPreviewCt().removeAll(true);
        var validationRuleSet = grid.lastSelected,
            rulesSetPreview = Ext.widget('deviceDataValidationRulesSetPreview', {
                rulesSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name')
            });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
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
    onRulesGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        grid.store.load({
            id: ruleSetId,
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesGridSelectionChange: function (grid) {
        var rulePreview = this.getRulePreview(),
            validationRule = grid.lastSelected,
            properties = validationRule.data.properties,
            readingTypes = validationRule.data.readingTypes;
        rulePreview.loadRecord(validationRule);
        rulePreview.setTitle(validationRule.get('name'));
        rulePreview.down('#propertiesArea').removeAll();
        for (var i = 0; i < properties.length; i++) {
            var property = properties[i];
            var propertyName = property.name;
            var propertyValue = property.value;
            var required = property.required;
            var label = propertyName;
            if (!required) {
                label = label + ' (optional)';
            }
            rulePreview.down('#propertiesArea').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: label,
                    value: propertyValue,
                    labelWidth: 260
                }
            );
        }
        rulePreview.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var readingType = readingTypes[i];
            var aliasName = readingType.aliasName;
            var mRID = readingType.mRID;
            var fieldlabel = Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading type(s)');
            if (i > 0) {
                fieldlabel = '&nbsp';
            }
            rulePreview.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 260,
                            width: 500,
                            value: mRID
                        },
                        {
                            xtype: 'component',
                            width: 500,
                            html: '<span style="color:grey"><i>' + aliasName + '</i></span>',
                            margin: '5 0 0 10'
                        }
                    ]
                }
            );
        }
    },
    changeRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getRulesSetGrid().getStore().getById(ruleSetId),
            ruleSetName = record.get('name'),
            ruleSetIsActive = record.get('isActive');
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            jsonData: (!ruleSetIsActive).toString(),
            success: function () {
                me.getRulesSetGrid().getStore().reload({
                    callback: function () {
                        me.getRulesSetGrid().getSelectionModel().doSelect(me.getRulesSetGrid().getStore().indexOf(record));
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translatePlural('device.dataValidation.ruleSet.deactivated', ruleSetName, 'MDC', 'Rule set {0} was deactivated successfully') :
                            Uni.I18n.translatePlural('device.dataValidation.ruleSet.activated', ruleSetName, 'MDC', 'Rule set {0} was activated successfully'));
                    }
                });
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
            title: Uni.I18n.translatePlural('device.dataValidation.activateConfirmation.title', me.mRID, 'MDC', 'Activate data validation on device {0}?'),
            msg: ''
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },
    activateDataValidation: function () {
        var me = this,
            isValidationRunImmediately = me.getValidationRunRg().getValue().validationRun === 'now';
        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: 'true',
            success: function () {
                me.updateDataValidationStatusSection();
                if (isValidationRunImmediately) {
                    me.validateData();
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation on device {0} was activated successfully'));
                }
            }
        });
    },
    validateData: function () {
        var me = this;
        me.getValidationProgress().add(Ext.create('Ext.ProgressBar', {
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
            jsonData: me.getValidationFromDate().getValue(),
            success: function () {
                me.destroyConfirmationWindow();
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation on device {0} was completed successfully'));
            },
            failure: function (response) {
                if (me.getActivationConfirmationWindow()) {
                    var res = Ext.JSON.decode(response.responseText);
                    me.getValidationProgress().removeAll(true);
                    me.showValidationActivationErrors(res.message);
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
    showDeactivationConfirmation: function () {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate')
        }).show({
                title: Uni.I18n.translatePlural('device.dataValidation.deactivateConfirmation.title', me.mRID, 'MDC', 'Deactivate data validation on device {0}?'),
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
            url: '/api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: 'false',
            success: function () {
                me.updateDataValidationStatusSection();
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.deactivation.successMsg', me.mRID, 'MDC', 'Data validation on device {0} was deactivated successfully'));
            }
        });
    },
    destroyConfirmationWindow: function () {
        if (this.getActivationConfirmationWindow()) {
            this.getActivationConfirmationWindow().removeAll(true);
            this.getActivationConfirmationWindow().destroy();
        }
    },
    confirmationWindowButtonsDisable: function (value) {
        if (this.getActivationConfirmationWindow()) {
            this.getActivationConfirmationWindow().down('button[name=confirm]').setDisabled(value);
            this.getActivationConfirmationWindow().down('button[name=cancel]').setDisabled(value);
        }
    },
    showValidationActivationErrors: function (errors) {
        if (this.getActivationConfirmationWindow()) {
            this.getValidationDateErrors().update(errors);
            this.getValidationDateErrors().setVisible(true);
        }
    },
    onValidationFromDateChange: function () {
        if (this.getActivationConfirmationWindow()) {
            this.getValidationDateErrors().update('');
            this.getValidationDateErrors().setVisible(false);
        }
    }
});