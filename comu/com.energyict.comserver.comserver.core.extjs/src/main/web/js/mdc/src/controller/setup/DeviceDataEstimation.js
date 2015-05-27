Ext.define('Mdc.controller.setup.DeviceDataEstimation', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.DeviceDataEstimationRulesSet',
        'Mdc.store.EstimationRules'
    ],
    stores: [
        'Mdc.store.DeviceDataEstimationRulesSet',
        'Mdc.store.EstimationRules'
    ],
    views: [
        'Mdc.view.setup.devicedataestimation.RulesSetMainView'
    ],
    refs: [
        {ref: 'page', selector: 'deviceDataEstimationRulesSetMainView'},
        {ref: 'rulesSetGrid', selector: 'deviceDataEstimationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataEstimationRulesSetPreviewCt'},
        {ref: 'rulePreview', selector: 'deviceDataEstimationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeEstimationRuleSetStateActionMenuItem'}
    ],
    mRID: null,
    device: null,
    init: function () {
        this.control({
            'deviceDataEstimationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                selectionchange: this.onRulesSetGridSelectionChange
            },
            'deviceDataEstimationRulesGrid': {
                afterrender: this.onRulesGridAfterRender,
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeEstimationRuleSetStateActionMenuItem': {
                click: this.changeRuleSetStatus
            },
            '#deviceDataEstimationStateChangeBtn': {
                click: this.changeDataEstimationStatus
            }
        });
        this.callParent();
    },
    showDeviceDataEstimationMainView: function (mRID) {
        var me = this;
        me.mRID = mRID;

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var view = Ext.widget('deviceDataEstimationRulesSetMainView', {device: device, router: me.getController('Uni.controller.history.Router')}),
                    status = device.get('estimationStatus').active,
                    toggleActivationButton = view.down('#deviceDataEstimationStateChangeBtn');

                me.device = device;
                me.getApplication().fireEvent('loadDevice', device);
                view.down('#deviceDataEstimationStatusField').setValue(status ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive'));
                if (toggleActivationButton) {
                    toggleActivationButton.setText((status ? Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                        Uni.I18n.translate('general.activate', 'MDC', 'Activate')) + ' ' + Uni.I18n.translate('estimationDevice.statusSection.buttonAppendix', 'MDC', 'data estimation'));
                    toggleActivationButton.action = status ? 'deactivate' : 'activate';
                }
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    changeDataEstimationStatus: function (btn) {
        var me = this,
            activate = btn.action === 'activate';

        Ext.create('Uni.view.window.Confirmation', {
            confirmText: activate ? Uni.I18n.translate('general.activate', 'MDC', 'Activate') : Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
            itemId: 'activationConfirmationWindow'
        }).show({
            title: activate ? Uni.I18n.translatePlural('estimationDevice.activateConfirmation.title', me.mRID, 'MDC', 'Activate data estimation on device {0}?') :
                Uni.I18n.translatePlural('estimationDevice.deactivateConfirmation.title', me.mRID, 'MDC', 'Deactivate data estimation on device {0}?'),
            msg: activate ? '' :
                Uni.I18n.translate('estimationDevice.deactivateConfirmation.msg', 'MDC', 'The data of this device will no longer be estimated'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.activateDataValidation(activate);
                }
            }
        });
    },

    activateDataValidation: function (activate) {
        var me = this;
        me.device.set('estimationStatus', { active: activate });
        me.getPage().setLoading();
        me.device.save({
            success: function () {
                var router = me.getController('Uni.controller.history.Router');
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', activate ?
                    Uni.I18n.translatePlural('estimationDevice.activation.activated', me.mRID, 'MDC', 'Data estimation on device {0} was activated successfully') :
                    Uni.I18n.translatePlural('estimationDevice.activation.deactivated', me.mRID, 'MDC', 'Data estimation on device {0} was deactivated successfully'));
            }
        });
    },

    onRulesSetGridAfterRender: function (grid) {
        grid.store.getProxy().setExtraParam('mRID', this.mRID);
        grid.store.load();
    },

    onRulesSetGridSelectionChange: function (grid) {
        this.getRulesSetPreviewCt().removeAll(true);
        var validationRuleSet = grid.lastSelected,
            rulesSetPreview = Ext.widget('deviceDataEstimationRulesSetPreview', {
                rulesSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name')
            }),
            changeRuleSetStateActionMenuItem = this.getChangeRuleSetStateActionMenuItem();
        this.getRulesSetPreviewCt().add(rulesSetPreview);
        if (changeRuleSetStateActionMenuItem) {
            changeRuleSetStateActionMenuItem.setText(validationRuleSet.get('active') ? Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') : Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
        }
    },

    onRulesGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        grid.store.getProxy().setUrl(ruleSetId);
        grid.store.load();
    },

    onRulesGridSelectionChange: function (grid) {
        var rulePreview = this.getRulePreview(),
            validationRule = grid.lastSelected,
            readingTypes = validationRule.data.readingTypes;

        Ext.suspendLayouts();
        rulePreview.loadRecord(validationRule);
        rulePreview.setTitle(validationRule.get('name'));
        rulePreview.down('property-form').loadRecord(validationRule);
        rulePreview.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                readingType = readingTypes[i];

            rulePreview.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 250,
                            value: readingType
                        }
                    ]
                }
            );
        }
        Ext.resumeLayouts(true);
    },

    changeRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getRulesSetGrid().getStore().getById(ruleSetId),
            ruleSetName = record.get('name'),
            ruleSetIsActive = record.get('active');

        record.getProxy().setExtraParam('mRID', me.mRID);
        record.set('active', !ruleSetIsActive);
        me.getPage().setLoading();
        record.save({
            success: function () {
                var router = me.getController('Uni.controller.history.Router');
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                    Uni.I18n.translatePlural('device.dataEstimation.ruleSet.deactivated', ruleSetName, 'MDC', 'Estimation rule set deactivated') :
                    Uni.I18n.translatePlural('device.dataEstimation.ruleSet.activated', ruleSetName, 'MDC', 'Estimation rule set activated'));
            }
        });
    }
});
