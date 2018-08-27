/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.controller.Purpose', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.view.summary.PurposeMain',
        'Imt.purpose.view.summary.validation.RulesSetGrid',
        'Imt.purpose.view.summary.estimation.EstimationRulesSetPreview',
        //'Imt.purpose.view.summary.estimation.EstimationRulesGrid'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.store.RegisterReadings',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.purpose.store.ValidationTasks',
        'Imt.purpose.store.EstimationTasks',
        'Imt.purpose.store.ExportTasks',
        'Imt.usagepointmanagement.store.Periods',
        'Imt.purpose.store.EstimationRules',
        'Imt.purpose.store.OutputValidationConfiguration',
        'Imt.purpose.store.OutputEstimationConfiguration',
        'Imt.purpose.store.PurposeSummaryData',
        'Imt.purpose.store.PurposeSummaryRegisterData',
        'Imt.purpose.store.FilteredOutputs',
        'Imt.purpose.store.IntervalFilter',
        'Imt.purpose.store.RegisterFilter',
        'Imt.purpose.store.UnitFilter',
        'Imt.purpose.store.PurposeValidationConfiguration',
        'Imt.purpose.store.PurposeValidationConfigurationStatus',
        'Imt.purpose.store.PurposeEstimationConfiguration'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.purpose.model.OutputValidationConfiguration',
        'Imt.purpose.model.OutputEstimationConfiguration'

    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm',
        'Imt.purpose.view.ValidationDate',
        'Uni.view.window.Confirmation',
        'Ext.ProgressBar',
        'Cfg.configuration.view.RuleWithAttributesEdit',
        'Imt.purpose.view.summary.validation.RulesSetMainView',
        'Imt.purpose.view.summary.validation.RulesSetMain',
        'Imt.purpose.view.summary.validation.RulePreview',
        'Imt.purpose.view.summary.validation.RulesSetActionMenu',
        'Imt.purpose.view.summary.estimation.EstimationRulesSetMainView',
        'Imt.purpose.view.summary.estimation.EstimationRulePreview',
        'Imt.purpose.view.summary.estimation.EstimationRulesGrid'
    ],

    refs: [
        {
            ref: 'purposeMain',
            selector: '#purpose-main'
        },
        {
            ref: 'purposeOverview',
            selector: '#purpose-overview'
        },
        {
            ref: 'purposePage',
            selector: '#purpose-outputs'
        },
        {
            ref: 'outputPreview',
            selector: '#purpose-outputs #output-preview'
        },
        {
            ref: 'purposeRegisterDataView',
            selector: 'purpose-register-data-view'
        },
        {
            ref: 'purposeRegisterDataPreviewPanel',
            selector: 'purpose-register-data-preview'
        },
        {
            ref: 'purposeIntervalDataPreviewPanel',
            selector: 'purpose-data-preview'
        },
        {
            ref: 'rulesSetGrid',
            selector: '#validationConfigurationRulesSetGrid'
        },
        {
            ref: 'rulesSetVersionPreviewCt',
            selector: '#validationConfigurationRuleSetVersionsPreviewCt'
        },
        {
            ref: 'ruleSetBrowsePreviewCt',
            selector: '#validationConfigurationRulesSetPreviewCt'
        },
        {
            ref: 'ruleSetsGrid',
            selector: 'validationConfigurationRulesGrid'
        },
        {
            ref: 'ruleItemPreviewCt',
            selector: '#ruleItemPreviewContainer'
        },

        {
            ref: 'ruleSetVersionPreview',
            selector: '#validationConfigurationRulesSetVersionPreview'
        },
        {
            ref: 'changeRuleSetStateActionMenuItem',
            selector: '#changeRuleSetStateActionMenuItem'
        },
        {
            ref: 'page',
            selector: '#validationConfigurationRulesSetMain'
        },
        {
            ref: 'estimationPage',
            selector: '#estimationCfgRulesSetMainView'
        },
        {
            ref: 'estimationRuleSetPreview',
            selector: '#estimationCfgRulesSetPreviewCt'
        },
        {
            ref: 'estimationRulePreview',
            selector: '#purpose-estimation-rule-preview'
        },
        {
            ref: 'estimationCfgSetGrid',
            selector: '#estimationCfgRulesSetGrid'
        },
        {
            ref: 'estimationCfgRulesGrid',
            selector: '#purpose-estimation-rules-grid'
        },
        {
            ref: 'rulePreview',
            selector: '#estimationCfgRulePreview'
        },
        {
            ref: 'changeEstimationRuleSetStateActionMenuItem',
            selector: '#changeEstimationRuleSetStateActionMenuItem'
        },
        {
            ref: 'estimationPage',
            selector: '#estimationCfgRulesSetMainView'
        },
        {
            ref: 'estimationStatusField',
            selector: '#estimationCfgStatusField'
        },
        {
            ref: 'validationButton',
            selector: '#validationConfigurationStateChangeBtn'
        },
        {
            ref: 'estimationCfgRuleSetView',
            selector: '#estimationCfgRulesSetPreview'

        },
        {
            ref: 'ruleEstimationPreview',
            selector: '#estimationCfgRulesSetPreview'
        },
        {
            ref: 'noEstimationPanel',
            selector: '#no-estimation-rule-sets'
        }

    ],

    hasEstimationRule: false,

    init: function () {
        this.control({
            'output-channel-main #readings-graph': {
                resize: this.onGraphResize
            },
            'purpose-outputs purpose-actions-menu': {
                click: this.chooseAction
            },
            '#purpose-outputs #outputs-list': {
                select: this.showOutputPreview
            },
            'purpose-register-data-view #purpose-register-data-grid': {
                select: this.loadPurposeRegisterDataPreview
            },
            'purpose-data-view #purpose-data-grid': {
                select: this.loadPurposeDataPreview
            },
            'validationConfigurationRulesSetMainView #validationConfigurationRulesSetGrid': {
                select: this.previewValidationRuleSet
            },
            'validationConfigurationRulesSetPreview #validationConfigurationRuleSetVersionsGrid': {
                select: this.previewVersionValidationRule
            },
            '#validationConfigurationRulesGrid': {
                select: this.previewValidationRule
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetStatus
            },
            '#validationConfigurationStateChangeBtn': {
                click: this.changeDataValidationStatus
            },
            '#validationFromDate': {
                change: this.onValidationFromDateChange
            },
            'estimationCfgRulesSetMainView #estimationCfgRulesSetGrid': {
                select: this.previewEstimationSetRule
            },
            'estimationCfgRulesSetPreview #purpose-estimation-rules-grid': {
                select: this.previewEstimationRule
            },

            '#changeEstimationRuleSetStateActionMenuItem': {
                click: this.changeEstimationRuleSetStatus
            },
            '#estimationCfgStateChangeBtn': {
                click: this.changeEstimationCfgStatus
            }
        });
    },

    changeDataValidationStatus: function (btn) {
        btn.action === 'activate' ? this.showActivationConfirmation(this.getPage()) : this.showDeactivationConfirmation(this.getPage());
    },
    changeEstimationCfgStatus: function (btn) {
        var me = this,
            activate = btn.action === 'activate';

        Ext.create('Uni.view.window.Confirmation', {
            confirmText: activate ? Uni.I18n.translate('general.activate', 'IMT', 'Activate') : Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate'),
            itemId: 'activationConfirmationWindow',
            green: activate
        }).show({
            title: activate
                ? Uni.I18n.translate('estimationPurpose.activateConfirmation.title', 'IMT', "Activate data estimation on purpose '{0}'?", me.getEstimationPage().purpose.data.name)
                : Uni.I18n.translate('estimationPurpose.deactivateConfirmation.title', 'IMT', "Deactivate data estimation on purpose '{0}'?", me.getEstimationPage().purpose.data.name),
            msg: activate ? '' : Uni.I18n.translate('estimationPurpose.deactivateConfirmation.msg', 'IMT', 'The data of this purpose will no longer be estimated'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.activateDataEstimation(activate);
                }
            }
        });

    },

    activateDataEstimation: function (activate) {
        var me = this,

            page = me.getEstimationPage();


        page.setLoading();

        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + page.usagePoint.getData().name + '/purposes/' + page.purpose.getData().id + '/estimationrulesets/estimationstatus',
            method: 'PUT',
            jsonData: {
                active: activate
            },
            isNotEdit: true,
            success: function () {
                me.updateEstimationCfgStatusSection();
                var router = me.getController('Uni.controller.history.Router');
                me.getApplication().fireEvent('acknowledge', activate
                    ? Uni.I18n.translate('estimationDevice.activation.activated', 'IMT', 'Data estimation activated')
                    : Uni.I18n.translate('estimationDevice.activation.deactivated', 'IMT', 'Data estimation deactivated')
                );
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    changeEstimationRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getEstimationCfgSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getEstimationCfgSetGrid().getStore().getById(ruleSetId),
            ruleSetName = record.get('name'),
            ruleSetIsActive = record.get('isActive'),
            page = me.getEstimationPage();


        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + page.usagePoint.getData().name + '/purposes/' + page.purpose.getData().id + '/estimationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                activeRules: record.get('activeRules'),
                description: record.get('description'),
                id: ruleSetId,
                isActive: !ruleSetIsActive,
                name: ruleSetName
            },
            success: function () {
                me.getEstimationCfgSetGrid().getStore().reload({
                    callback: function () {
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translate('estimationCfg.ruleSet.deactivated', 'IMT', 'Estimation rule set deactivated') :
                            Uni.I18n.translate('estimationCfg.ruleSet.activated', 'IMT', 'Estimation rule set activated'));
                    }
                });
            }
        });

    },

    showActivationConfirmation: function (view) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'activationConfirmationWindow',
                green: true,
                confirmBtnUi: 'action',
                confirmText: Uni.I18n.translate('general.activate', 'IMT', 'Activate'),
                confirmAndRunText: Uni.I18n.translate('general.activateAndRun', 'IMT', 'Activate & Run'),
                confirmation: function () {
                    me.activateDataValidation(view, this, false);
                },
                confirmationAndRun: function () {
                    me.activateDataValidation(view, this, true);
                }

            });
        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/validationrulesets/validationstatus',
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

                if (res.hasValidation) {
                    confirmationWindow.insert(1, me.getActivationConfirmationContent());

                    // remove confirm and cancel buttons
                    var buttonConfirm = confirmationWindow.down('button[name=confirm]'),
                        buttonCancel = confirmationWindow.down('button[name=cancel]'),
                        owner = buttonConfirm.ownerCt;

                    owner.remove(buttonConfirm);
                    owner.remove(buttonCancel);

                    // add 3 new buttons
                    owner.insert(1, me.getButtonContent(confirmationWindow));

                    confirmationWindow.show({
                        title: Uni.I18n.translate('validationConfiguration.activateConfirmation.title', 'IMT', 'Activate data validation on purpose {0}?', me.getPage().purpose.getData().name)
                    });
                } else {
                    confirmationWindow.show({
                        title: Uni.I18n.translate('validationConfiguration.activateConfirmation.title', 'IMT', 'Activate data validation on purpose {0}?', me.getPage().purpose.getData().name),
                        msg: Uni.I18n.translate('validationConfiguration.activateMsg', 'IMT', 'There are currently no readings for this purpose.')
                    });
                }
            }
        });
        confirmationWindow.on('close', function () {
            this.destroy();
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
                    fieldLabel: Uni.I18n.translate('validationConfiguration.activateConfirmation.item', 'IMT', 'Validate data from'),
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
            confirmText: Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate')
        }).show({
            title: Uni.I18n.translate('validationConfiguration.deactivateConfirmation.title', 'IMT', 'Deactivate data validation on purpose {0}?', [me.getPage().purpose.getData().name]),
            msg: Uni.I18n.translate('validationConfiguration.deactivateConfirmation.msg', 'IMT', 'The data of this purpose will no longer be validated'),
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
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/validationrulesets/validationstatus',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                validationActive: 'false'
            },
            success: function () {
                me.updateValidationConfigurationStatusSection();
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('validationConfiguration.deactivation.successMsg', 'IMT', 'Data validation deactivated'));
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

    activateDataValidation: function (view, confWindow, runNow) {
        var me = this;

        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/validationrulesets/validationstatus',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                validationActive: 'true',
                lastChecked: (me.hasValidation ? confWindow.down('#validationFromDate').getValue().getTime() : new Date().getTime()),
            },
            success: function (record) {
                me.updateValidationConfigurationStatusSection();
                if (runNow) {
                    me.isValidationRunImmediately = true;
                    me.validateData(confWindow, record);
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationConfiguration.activation.activated', 'IMT', 'Data validation activated'));
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
    confirmationWindowButtonsDisable: function (value) {
        var activationConfirmationWindow;

        activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
            var button = activationConfirmationWindow.down('button[name=confirm]');

            button = activationConfirmationWindow.down('button[name=confirm]');
            if (button) {
                button.setDisabled(value);
            }

            button = activationConfirmationWindow.down('button[name=confirmAndRun]');
            if (button) {
                button.setDisabled(value);
            }

            button = activationConfirmationWindow.down('button[name=cancel]');
            if (button) {
                button.setDisabled(value);
            }
        }
    },

    validateData: function (confWindow, record) {
        var me = this,
            view = me.getPage();

        confWindow.down('#validationProgress').add(Ext.create('Ext.ProgressBar', {
            margin: '5 0 15 0'
        })).wait({
            duration: 120000,
            text: Uni.I18n.translate('validationConfiguration.isInProgress', 'IMT', 'Data validation is in progress. Please wait...'),
            fn: function () {
                me.destroyConfirmationWindow();
                Ext.widget('messagebox', {
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
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
                    title: Uni.I18n.translate('validationConfiguration.timeout.title1', 'IMT', 'Data validation takes longer than expected'),
                    msg: Uni.I18n.translate('validationConfiguration.timeout.message', 'IMT', 'Data validation takes longer than expected and will continue in the background.'),
                    icon: Ext.MessageBox.ERROR
                });
            }
        });
        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/validate',
            method: 'PUT',
            timeout: 600000,
            isNotEdit: true,
            jsonData: {
                name: view.purpose.get('name'),
                id: view.purpose.getId(),
                parent: {
                    id: view.usagePoint.get('id'),
                    version: view.usagePoint.get('version')
                },
                validationInfo: view.purpose.getRecordData().validationInfo,
                version: view.purpose.getRecordData().version

            },
            success: function () {
                me.destroyConfirmationWindow();
                if (me.isValidationRunImmediately) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('validationConfiguration.activation.validated', 'IMT', 'Data validation completed'));
                } else {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('validationConfiguration.activation.activated', 'IMT', 'Data validation activated'));
                }
            },
            failure: function (response) {
                var res;

                if (confWindow) {
                    if (response.status === 400) {
                        res = Ext.JSON.decode(response.responseText);
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
    destroyConfirmationWindow: function () {
        var activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
            activationConfirmationWindow.removeAll(true);
            activationConfirmationWindow.destroy();
        }
    },
    showValidationActivationErrors: function (errors) {
        var activationConfirmationWindow, validationDateErrors;

        activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
            validationDateErrors = Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors');
            if (validationDateErrors) {
                validationDateErrors.update(errors);
                validationDateErrors.setVisible(true);
            }
        }
    },
    onValidationFromDateChange: function () {
        var activationConfirmationWindow, validationDateErrors;

        activationConfirmationWindow = Ext.ComponentQuery.query('#activationConfirmationWindow')[0];
        if (activationConfirmationWindow) {
            validationDateErrors = Ext.ComponentQuery.query('#activationConfirmationWindow')[0].down('#validationDateErrors');
            if (validationDateErrors) {
                validationDateErrors.update('');
                validationDateErrors.setVisible(false);
            }
        }
    },

    changeRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getRulesSetGrid().getStore().getById(ruleSetId),
            ruleSetIsActive = record.get('isActive'),
            page = me.getPage();

        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + page.usagePoint.getData().name + '/purposes/' + page.purpose.getData().id + '/validationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: !ruleSetIsActive
            },
            success: function (res) {
                var data = Ext.decode(res.responseText);
                //page.device.set(data.device);
                me.getRulesSetGrid().getStore().reload({
                    callback: function () {
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translate('validationConfiguration.ruleSet.deactivated', 'IMT', 'Validation rule set deactivated') :
                            Uni.I18n.translate('validationConfiguration.ruleSet.activated', 'IMT', 'Validation rule set activated'));
                    }
                });
            }
        });
    },

    previewValidationRule: function (grid, record) {
        Ext.suspendLayouts();

        var me = this,
            itemPanel = me.getRuleSetVersionPreview(),
            itemForm = itemPanel.down('#ruleItemPreviewContainer'),
            selectedRule;
        if (grid.view) {
            selectedRule = grid.view.getSelectionModel().getLastSelected();
        } else {
            selectedRule = record;
        }
        me.ruleId = selectedRule.internalId;


        itemForm.updateValidationRule(selectedRule);
        Ext.resumeLayouts();
    },

    previewValidationRuleSet: function (selectionModel, record) {
        if (record) {
            Ext.suspendLayouts();

            this.getRuleSetBrowsePreviewCt().removeAll(true);
            var rulesPreviewContainerPanel = Ext.widget('validationConfigurationRulesSetPreview', {
                ruleSetId: record.getId(),
                title: record.get('name'),
                isSecondPagination: true
            });
            this.ruleSetId = record.getId();
            Ext.Array.each(Ext.ComponentQuery.query('#addRuleLink'), function (item) {
                item.hide();
            });
            this.getRuleSetBrowsePreviewCt().add(rulesPreviewContainerPanel);
            var menuItem = this.getChangeRuleSetStateActionMenuItem();
            if (!!menuItem) {
                menuItem.setText(record.get('isActive') ?
                    Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate') :
                    Uni.I18n.translate('general.activate', 'IMT', 'Activate'))
            }

            Ext.resumeLayouts(true);
        }
    },
    previewEstimationSetRule: function (selectionModel, record) {

        var me = this,
            estimationCfgRuleStore,
            ruleSetId = selectionModel.getLastSelected().get('id');

        if (record) {
            Ext.suspendLayouts();

            this.getEstimationRuleSetPreview().removeAll(true);
            var estimationCfgPreviewPanel = Ext.widget('estimationCfgRulesSetPreview', {
                ruleSetId: ruleSetId,
                title: record.get('name'),
                ui: 'medium',
                padding: 0,
                isSecondPagination: true
            });
            this.getEstimationRuleSetPreview().add(estimationCfgPreviewPanel);
            var menuItem = this.getChangeEstimationRuleSetStateActionMenuItem();
            if (!!menuItem) {
                menuItem.setText(record.get('isActive') ?
                    Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate') :
                    Uni.I18n.translate('general.activate', 'IMT', 'Activate'))
            }
            estimationCfgRuleStore = Ext.getStore('Imt.store.EstimationRules');
            estimationCfgRuleStore.getProxy().extraParams = {
                ruleSetId: ruleSetId,
            };
            estimationCfgRuleStore.load();


            Ext.resumeLayouts(true);
        }

    },

    previewEstimationRule: function (grid, record) {

        Ext.suspendLayouts();

        var me = this,
            itemPanel = me.getRuleEstimationPreview(),
            itemForm = itemPanel.down('#purpose-estimation-rule-preview'),
            selectedRule;
        if (grid.view) {
            selectedRule = grid.view.getSelectionModel().getLastSelected();
        } else {
            selectedRule = record;
        }
        me.ruleId = selectedRule.internalId;


        itemForm.updateEstimationPreview(selectedRule);
        Ext.resumeLayouts(true);


    },


    previewVersionValidationRule: function (selectionModel, record) {
        var me = this;

        if (record) {
            Ext.suspendLayouts();

            this.getRulesSetVersionPreviewCt().removeAll(true);
            var versionValidationRulesPreviewContainerPanel = Ext.widget('validationConfigurationRulesSetVersionPreview', {
                ruleSetId: me.ruleSetId,
                versionId: record.getId(),
                title: record.get('name'),
                ui: 'medium',
                padding: 0,
                isSecondPagination: true
            });
            this.versionId = record.getId();
            if (me.getRuleSetsGrid()) {
                Ext.Array.each(Ext.ComponentQuery.query('#newVersion'), function (item) {
                    item.hide();
                });
                Ext.Array.each(Ext.ComponentQuery.query('#addRuleLink'), function (item) {
                    item.hide();
                });
            }

            this.getRulesSetVersionPreviewCt().add(versionValidationRulesPreviewContainerPanel);

            Ext.resumeLayouts(true);
            this.getRulesSetVersionPreviewCt().doLayout();
        }

    },

    loadOutputs: function (usagePointId, purposeId, callback) {
        var me = this,
            outputsStore = me.getStore('Imt.purpose.store.Outputs');

        outputsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        outputsStore.load(callback);
    },

    showOutputs: function (usagePointId, purposeId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            periodsStore = me.getStore('Imt.usagepointmanagement.store.Periods'),
            intervalsStore = me.getStore('Imt.purpose.store.IntervalFilter'),
            registersStore = me.getStore('Imt.purpose.store.RegisterFilter'),
            unitsStore = me.getStore('Imt.purpose.store.UnitFilter'),
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            extraParams = {
                usagePointId: usagePointId,
                purposeId: purposeId
            },
            dependenciesCounter = 7,
            defaultPeriod,
            usagePoint,
            intervalsCount,
            registersCount,
            purposes;

        if (!router.queryParams.purposeTab) {
            window.location.replace(router.getRoute('usagepoints/view/purpose').buildUrl(null,{purposeTab: 'overview'}));
        } else {
            mainView.setLoading();

            me.getStore('Imt.purpose.store.ValidationTasks').getProxy().extraParams = extraParams;
            me.getStore('Imt.purpose.store.EstimationTasks').getProxy().extraParams = extraParams;
            me.getStore('Imt.purpose.store.ExportTasks').getProxy().extraParams = extraParams;
            me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(onDependenciesLoad);
            var filteredOutputsStore = me.getStore('Imt.purpose.store.FilteredOutputs');
            filteredOutputsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};

            intervalsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            intervalsStore.load(function (records) {
                intervalsCount = records.length;
                onDependenciesLoad();
            });

            registersStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            registersStore.load(function (records) {
                registersCount = records.length;
                onDependenciesLoad();
            });

            unitsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            unitsStore.load(function (records) {
                onDependenciesLoad();
            });

            periodsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            periodsStore.load(function (records) {
                defaultPeriod = records[0].getId();
                onDependenciesLoad();
            });

            me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointId, {
                success: function (record) {
                    usagePoint = record;
                    app.fireEvent('usagePointLoaded', usagePoint);
                    onDependenciesLoad();
                }
            });

            purposesStore.getProxy().extraParams = {usagePointId: usagePointId};
            purposesStore.load(function (records) {
                purposes = records;
                app.fireEvent('purposes-loaded', purposes);
                onDependenciesLoad();
            });



            function onDependenciesLoad() {
                var purpose,
                    widget;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    usagePoint.set('purposes', purposes);
                    purpose = _.find(purposes, function (p) {
                        return p.getId() == purposeId
                    });

                    widget = Ext.widget('purpose-main', {
                        itemId: 'purpose-main',
                        router: router,
                        usagePoint: usagePoint,
                        outputs: filteredOutputsStore,
                        purposes: purposes,
                        purpose: purpose,
                        intervalsCount: intervalsCount,
                        registersCount: registersCount,
                        defaultPeriod: defaultPeriod,
                        controller: me,
                        prevNextListLink: me.makeLinkToOutputs(router),
                        tab: router.queryParams.purposeTab
                    });

                    app.fireEvent('changecontentevent', widget);
                    if (mainView.down('purpose-actions-menu')) {
                        mainView.down('purpose-actions-menu').record = purpose;
                    }
                    mainView.setLoading(false);
                    me.loadOutputs(usagePointId, purposeId);
                }
            }
        }
    },


    showOverviewTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.purposeTab != 'overview') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.queryParams.purposeTab = 'overview';
            router.getRoute('usagepoints/view/purpose').forward();
        }

        panel.down('#purpose-details-form').loadRecord(panel.purpose);
    },

    showDataViewTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.purposeTab != 'data-view') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.queryParams.purposeTab = 'data-view';
            router.getRoute('usagepoints/view/purpose').forward();
        }

        var readingsStore = me.getStore('Imt.purpose.store.PurposeSummaryData');
        readingsStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId()
        };
        readingsStore.load();
    },

    showRegisterDataViewTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            registerDataStore;

        if (router.arguments.tab != 'registerData') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.queryParams.tab = 'registerData';
            router.getRoute('usagepoints/view/purpose').forward();
        }

        registerDataStore = Ext.getStore('Imt.purpose.store.PurposeSummaryRegisterData');
        registerDataStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId()
        };
        registerDataStore.load();
    },

    showValidationConfigurationTab: function (panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            validationConfigurationStore,
            purpose,
            view = me.getPage();


        if (router.arguments.tab != 'validationCfg') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.queryParams.tab = 'validationCfg';
            router.getRoute('usagepoints/view/purpose').forward();
        }


        validationConfigurationStore = Ext.getStore('Imt.purpose.store.PurposeValidationConfiguration');
        validationConfigurationStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId()
        };
        validationConfigurationStore.load({
            callback: function (records, operation, success) {
                if (records.length == 0) {
                    view.down('#validationConfigurationStateChangeBtn').setDisabled(true);
                }
            }

        });

        me.updateValidationConfigurationStatusSection();
    },

    showEstimationCfgTab: function (panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            estimationCfgStore,
            view = me.getEstimationPage();


        if (router.arguments.purposeTab != 'estimationCfg') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.queryParams.purposeTab = 'estimationCfg';
            router.getRoute('usagepoints/view/purpose').forward();
        }


        estimationCfgStore = Ext.getStore('Imt.purpose.store.PurposeEstimationConfiguration');
        estimationCfgStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId()
        };
        estimationCfgStore.load({
            callback: function (records, operation, success) {
                if (records.length == 0) {
                    view.down('#estimationCfgStateChangeBtn').setDisabled(true);
                }
            }

        });


        me.updateEstimationCfgStatusSection();

    },
    updateEstimationCfgStatusSection: function () {

        var me = this,
        view = me.getEstimationPage();

        if (view.down('#estimationCfgStatusField')) {
            view.down('#estimationCfgStatusPanel').setLoading(true);

            // !!view.down('#estimationCfgStateChangeBtn') && view.down('#estimationCfgStateChangeBtn').setDisabled(true);
        }

        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/estimationrulesets/estimationstatus',
            method: 'GET',
            timeout: 60000,
            callback: function () {
                var estimationCfgStatusPanel = view.down('#estimationCfgStatusPanel');

                if (estimationCfgStatusPanel) {
                    estimationCfgStatusPanel.setLoading(false);
                }
            },
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);

                if (view.down('#estimationCfgStatusPanel')) {
                    view.down('#estimationCfgStatusField').setValue(res.active);
                    if (!!view.down('#estimationCfgStateChangeBtn')) {
                        view.down('#estimationCfgStateChangeBtn').setText((res.active ?
                                Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate') :
                                Uni.I18n.translate('general.activate', 'IMT', 'Activate')) +
                            ' ' + Uni.I18n.translate('estimationCfg.statusSection.buttonAppendix', 'IMT', 'data estimation')
                        );
                        view.down('#estimationCfgStateChangeBtn').action = res.active ? 'deactivate' : 'activate';
                        //view.down('#estimationCfgStateChangeBtn').setDisabled(false);

                    }

                }
            }
        });

    },

    updateValidationConfigurationStatusSection: function () {
        var me = this,
            purposesStore,
            purposes,
            purpose,

            view = me.getPage(),
            purposeOverview = me.getPurposeOverview(),
            statusValidationBtn = view.down('#validationConfigurationStateChangeBtn'),
            element = me.getRuleSetsGrid();

        purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            purposesStore.getProxy().extraParams = {usagePointId: view.usagePoint.get('name')};
        purposesStore.load(function (record) {
            purposes = record;
            purpose = _.find(purposes, function (p) {
                return p.getId() == view.purpose.getId();
            });
            purposeOverview.purpose = purpose;
        });


        if (view.down('#validationConfigurationStatusField')) {
            view.down('#validationConfigurationStatusPanel').setLoading(true);
            //!!view.down('#validationConfigurationStateChangeBtn') && view.down('#validationConfigurationStateChangeBtn').setDisabled(true);
        }

        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + view.usagePoint.get('name') + '/purposes/' + view.purpose.getId() + '/validationrulesets/validationstatus',
            method: 'GET',
            timeout: 60000,

            callback: function () {
                var validationConfigurationStatusPanel = view.down('#validationConfigurationStatusPanel');

                if (validationConfigurationStatusPanel) {
                    validationConfigurationStatusPanel.setLoading(false);
                }

            },
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (view.down('#validationConfigurationStatusPanel')) {
                    view.down('#validationConfigurationStatusField').setValue(res.validationActive);
                    if (!!view.down('#validationConfigurationStateChangeBtn')) {
                        view.down('#validationConfigurationStateChangeBtn').setText((res.validationActive ?
                                Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate') :
                                Uni.I18n.translate('general.activate', 'IMT', 'Activate')) +
                            ' ' + Uni.I18n.translate('validationConfiguration.statusSection.buttonAppendix', 'IMT', 'data validation')
                        );
                        view.down('#validationConfigurationStateChangeBtn').action = res.validationActive ? 'deactivate' : 'activate';
                        // if (!me.getRuleSetsGrid()) {
                        //     statusValidationBtn.setDisabled(true);
                        // }
                    }

                }
            }
        });
    },



    showOutputPreview: function (selectionModel, record) {
        var me = this;

        me.getOutputPreview().loadRecord(record);
    },

    makeLinkToOutputs: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.outputs', 'IMT', 'Outputs').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('usagepoints/view/purpose').buildUrl());
    },

    showOutputDefaultTab: function(usagePointId, purposeId, outputId, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            prevNextListLink = me.makeLinkToOutputs(router),
            outputModel,
            dependenciesCounter,
            displayPage,
            usagePoint,
            purposes,
            purpose,
            outputs,
            output,
            validationConfigurationStore = me.getStore('Imt.purpose.store.OutputValidationConfiguration'),
            estimationConfigurationStore = me.getStore('Imt.purpose.store.OutputEstimationConfiguration');

        if (!tab || (tab === 'validation' && !Imt.privileges.UsagePoint.canViewValidationConfiguration()) ||
                    (tab === 'estimation' && !Imt.privileges.UsagePoint.canViewEstimationConfiguration())) {
            window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
        } else {
            validationConfigurationStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};
            validationConfigurationStore.load(function () {
                displayPage();
            });

            estimationConfigurationStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};
            estimationConfigurationStore.load(function () {
                displayPage();
            });

            outputModel = me.getModel('Imt.purpose.model.Output');
            dependenciesCounter = 5;
            displayPage = function () {
                var widget;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    if ((tab === 'validation' && !validationConfigurationStore.getCount()) || (tab === 'estimation' && output.get('outputType') === 'channel' && !estimationConfigurationStore.getCount())) {
                        window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
                        return;
                    }
                    mainView.setLoading(false);
                    app.fireEvent('outputs-loaded', outputs);
                    app.fireEvent('output-loaded', output);
                    widget = Ext.widget('output-channel-main', {
                        itemId: 'output-channel-main',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        purpose: purpose,
                        outputs: outputs,
                        output: output,
                        interval: me.getInterval(output),
                        prevNextListLink: prevNextListLink,
                        controller: me,
                        validationConfigurationStore: validationConfigurationStore,
                        estimationConfigurationStore: estimationConfigurationStore,
                        tab: tab
                    });
                    app.fireEvent('changecontentevent', widget);
                    widget.down('output-specifications-form').loadRecord(output);
                }
            };

            mainView.setLoading();

            usagePointsController.loadUsagePoint(usagePointId, {
                success: function (types, up, records) {
                    usagePoint = up;
                    purposes = records;
                    purpose = _.find(records, function (p) {
                        return p.getId() == purposeId
                    });
                    displayPage();
                }
            });

            // avoid handling events of the outputs store by components on the purpose page
            var outputsStore = me.getStore('Imt.purpose.store.Outputs');
            outputsStore.suspendEvents();
            me.loadOutputs(usagePointId, purposeId, function (records) {
                outputsStore.resumeEvents();
                outputs = records;
                displayPage();
            });
            outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            outputModel.load(outputId, {
                success: function (record) {
                    output = record;
                    if(record.get('outputType') === "channel"){
                        var estimationRulesStore = me.getStore('Imt.purpose.store.EstimationRules');
                        estimationRulesStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId}
                        estimationRulesStore.load(function(records){
                            me.hasEstimationRule = Boolean(records.length);
                            displayPage();
                        });
                    } else {
                        displayPage();
                    }
                }
            });
        }
    },

    getInterval: function(output) {
        var me = this,
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels');

        return intervalStore.getIntervalRecord(output.get('interval'));
    },

    showSpecificationsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'specifications') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'specifications';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showValidationTab: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'validation') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'validation';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showEstimationTab: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'estimation') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'estimation';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showReadingsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            output = panel.output,
            readingsStore;
        switch (output.get('outputType')) {
            case 'channel':
                readingsStore = me.getStore('Imt.purpose.store.Readings');
                break;
            case 'register':
                readingsStore = me.getStore('Imt.purpose.store.RegisterReadings');
                break;
        }

        if (router.arguments.tab != 'readings') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'readings';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }

        readingsStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId(),
            outputId: panel.output.getId()
        };

        readingsStore.load();
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },

    chooseAction: function (menu, item) {
        var me = this;

        if (item) {
            switch (item.action) {
                case 'validateNow':
                    me.validatePurpose(menu.record);
                    break;
            }
            switch (item.action) {
                case 'estimateNow':
                    me.estimatePurpose(menu.record);
                    break;
            }
        }
    },

    validatePurpose: function (purpose) {
        var me = this,
            usagePoint = Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint,
            lastChecked = purpose.get('validationInfo').lastChecked,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'purpose-validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'IMT', 'Validate'),
                closeAction: 'destroy',
                green: true,
                confirmation: Ext.bind(me.onValidateNow, me, [purpose, usagePoint, getConfirmationWindow])
            });

        confirmationWindow.insert(1, {
            xtype: 'validation-date',
            itemId: 'purpose-rdg-validation-run',
            defaultDate: lastChecked ? new Date(lastChecked) : new Date(),
            padding: '-10 0 0 45'
        });
        confirmationWindow.insert(1, {
            itemId: 'validate-now-window-errors',
            xtype: 'label',
            margin: '0 0 10 50',
            hidden: true
        });
        confirmationWindow.show({
            title: Uni.I18n.translate('purpose.validateNow', 'IMT', "Validate data for '{0}' purpose on usage point '{1}'?",
                [purpose.get('name'), usagePoint.get('name')], false)
        });

        function getConfirmationWindow() {
            return confirmationWindow
        }
    },

    onValidateNow: function (purpose, usagePoint, getConfirmationWindow) {
        var me = this,
            confWindow = getConfirmationWindow(),
            progressbar = confWindow.insert(2, {
                xtype: 'progressbar',
                itemId: 'validation-progressbar',
                margin: '5 0 15 0'
            }),
            lastChecked;

        progressbar.wait({
            duration: 60000,
            fn: Ext.bind(me.onTooLongOperation, me, [purpose, confWindow, {
                title: Uni.I18n.translate('purpose.dataValidation.timeout.title1', 'IMT', 'Data validation takes longer than expected'),
                msg: Uni.I18n.translate('purpose.dataValidation.timeout.message', 'IMT', 'Data validation takes longer than expected and will continue in the background.')
            }])
        });

        if (confWindow.down('#purpose-rdg-validation-run').getValue().validation === 'newValidation') {
            lastChecked = confWindow.down('#purpose-dtm-validation-from-date').getValue().getTime();
        } else if (purpose.get('validationInfo').lastChecked) {
            lastChecked = purpose.get('validationInfo').lastChecked;
        } else {
            lastChecked = new Date().getTime();
        }
        purpose.set('validationInfo', {lastChecked: lastChecked});
        me.doOperation(purpose, usagePoint, confWindow, Uni.I18n.translate('purpose.successMsg', 'IMT', 'Data validation for the purpose is completed'), 'validate');
    },

    estimatePurpose: function (purpose) {
        var me = this,
            usagePoint = Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint,
            confirmationWindow = Ext.widget('confirmation-window', {
                itemId: 'estimate-now-confirmation-window',
                closeAction: 'destroy',
                confirmText: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
                confirmation: _.once(Ext.bind(me.onEstimateNow, me, [purpose, usagePoint, getConfirmationWindow])),
                green: true
            });

        confirmationWindow.insert(1, {
            xtype: 'fieldcontainer',
            margin: '0 10 10 50',
            flex: 1,
            layout: 'hbox',
            items: [
            {
                xtype: 'checkbox',
                itemId: 'revalidate'
            },
            {
                xtype: 'label',
                margin: 6,
                text: Uni.I18n.translate('purpose.revalidationEstimatedData', 'IMT', 'Re-validate estimated data')
            }
        ]
        });

        confirmationWindow.show({
            title: Uni.I18n.translate('purpose.estimateData', 'IMT', "Estimate data of '{0}' purpose on usage point '{1}'?",
                [purpose.get('name'), usagePoint.get('name')], false)
        });

        function getConfirmationWindow() {
            return confirmationWindow
        }
    },

    onEstimateNow: function (purpose, usagePoint, getConfirmationWindow) {
        var me = this,
            confirmationWindow = getConfirmationWindow(),
            progressbar = confirmationWindow.insert(2, {
                xtype: 'progressbar',
                itemId: 'estimation-progressbar',
                margin: '5 0 15 0'
            });

        progressbar.wait({
            duration: 60000,
            fn: Ext.bind(me.onTooLongOperation, me, [purpose, confirmationWindow, {
                title: Uni.I18n.translate('purpose.dataEstimation.timeout.title1', 'IMT', 'Data estimation takes longer than expected'),
                msg: Uni.I18n.translate('purpose.dataEstimation.timeout.message', 'IMT', 'Data estimation takes longer than expected and will continue in the background.')
            }])
        });
        purpose.set('revalidate', confirmationWindow.down('#revalidate').getValue());
        me.doOperation(purpose, usagePoint, confirmationWindow, Uni.I18n.translate('purpose.dataEstimation.successMsg', 'IMT', 'Data estimation for the purpose is completed'), 'estimate');
    },

    onTooLongOperation: function (purpose, confirmationWindow, errorMessageConfig) {
        var errorMessage = Ext.widget('messagebox', {
            closeAction: 'destroy',
            buttons: [
                {
                    text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                    ui: 'remove',
                    handler: function () {
                        this.up('window').close();
                    }
                }
            ]
        });

        Ext.suspendLayouts();
        confirmationWindow.close();
        errorMessage.show(Ext.apply({
            ui: 'notification-error',
            modal: false,
            icon: Ext.MessageBox.ERROR
        }, errorMessageConfig));
        Ext.resumeLayouts(true);
    },

    doOperation: function (purpose, usagePoint, confirmationWindow, successMessage, action) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            formErrorsPanel = confirmationWindow.down('#validate-now-window-errors'),
            progressbar = confirmationWindow.down('progressbar'),
            purposePage = me.getPurposePage();

        purpose[action](usagePoint, {
            isNotEdit: true,
            notHandleTimeout: true,
            callback: function (options, success, response) {
                if (response.status && success) {
                    confirmationWindow.close();
                }
                if (purposePage.rendered && success) {
                    me.getApplication().fireEvent('acknowledge', successMessage);
                    router.getRoute().forward(null, Ext.Object.fromQueryString(router.getQueryString()));
                }
                if(!success){
                    if(formErrorsPanel){
                        var responseText = Ext.decode(response.responseText, true);
                        Ext.suspendLayouts();
                        formErrorsPanel.setText('<div style="color: #EB5642">' + responseText.errors[0].msg + '</div>', false);
                        formErrorsPanel.show();
                        progressbar.reset(true);
                        Ext.resumeLayouts(true);
                    }
                }
            }
        });
    },

    showEditValidationRuleWithAttributes: function() {
        this.showEditRuleWithAttributes('validation');
    },

    showEditEstimationRuleWithAttributes: function() {
        this.showEditRuleWithAttributes('estimation');
    },

    showEditRuleWithAttributes: function(type) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointId = router.arguments.usagePointId,
            purposeId = router.arguments.purposeId,
            outputId = router.arguments.outputId,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            dependenciesCounter = 3,
            outputModel = me.getModel('Imt.purpose.model.Output'),
            ruleWithAttributesModel = type === 'validation' ? me.getModel('Imt.purpose.model.OutputValidationConfiguration') : me.getModel('Imt.purpose.model.OutputEstimationConfiguration'),
            form,
            output,
            rule,
            widget;

        mainView.setLoading();
        outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        ruleWithAttributesModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};

        usagePointsController.loadUsagePoint(usagePointId, {
            success: function () {
                displayPage();
            }
        });
        outputModel.load(outputId, {
            success: function (record) {
                output = record;
                displayPage();
            }
        });
        ruleWithAttributesModel.load(router.arguments.ruleId, {
            success: function (record) {
                rule = record;
                displayPage();
            }
        });

        function displayPage() {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                mainView.setLoading(false);
                app.fireEvent('output-loaded', output);
                widget = Ext.widget('rule-with-attributes-edit', {
                    itemId: 'rule-with-attributes-edit-' + type,
                    type: type,
                    route: router.getRoute('usagepoints/view/purpose/output'),
                    application: me.getApplication()
                });
                form = widget.down('#rule-with-attributes-edit-form');
                form.loadRecord(rule);
                form.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'IMT', 'Edit attributes for'), rule.get('name')));
                form.down('property-form').loadRecord(rule);
                app.fireEvent('rule-with-attributes-loaded', rule);
                app.fireEvent('changecontentevent', widget);
            }
        }
    },


    getSelectedRegisterOutput: function(registerData){
        var outputsStore = this.getStore('Imt.purpose.store.Outputs'),
            outputId = registerData.get('output').id;
        return outputsStore.findRecord('id', outputId);
    },

    loadPurposeRegisterDataPreview: function (selectionModel, record) {
        if (selectionModel.getSelection().length === 1) {
            var output = this.getSelectedRegisterOutput(record);
            this.getPurposeRegisterDataPreviewPanel().updateForm(record, output);
        }
    },

    loadPurposeDataPreview: function(selectionModel, record){
        var filteredOutputsStore = this.getStore('Imt.purpose.store.FilteredOutputs');
        if (!filteredOutputsStore.isLoading()) {
            if (selectionModel.getSelection().length === 1) {
                this.getPurposeIntervalDataPreviewPanel().updateForm(record);
            }
        }
    }
});