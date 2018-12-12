/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset', {
    extend: 'Ext.container.Container',
    alias: 'widget.mdc-device-validation-results-ruleset',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    store: 'Mdc.store.DeviceConfigurationResults',
    requires: [
        'Mdc.view.setup.devicevalidationresults.RuleSetList',
        'Mdc.view.setup.devicevalidationresults.RuleSetVersionList',
        'Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList',
        'Mdc.view.setup.devicedatavalidation.RulePreview',
        'Mdc.store.DeviceConfigurationResults'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    router: null,
    loadProfiles: null,
    intervalRegisterStart: null,
    intervalRegisterEnd: null,
    mainView: null,

    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);
        this.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'form',
                        itemId: 'frm-device-validation-results-ruleset',
                        flex: 1,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150,
                            labelAlign: 'left'
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'dpl-configuration-view-data-validated',
                                fieldLabel: Uni.I18n.translate('validationResults.allDataValidated', 'MDC', 'All data validated'),
                                name: 'dataValidatedDisplay',
                                value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...'),
                                htmlEncode: false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'dpl-configuration-view-validation-results',
                                fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
                                name: 'total',
                                value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...')
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'bottom',
                            pack: 'end'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'btn-configuration-view-validate-now',
                                text: Uni.I18n.translate('validationResults.validateNow', 'MDC', 'Validate now'),
                                disabled: true,
                                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions,
                                action: 'validateNow'
                            }
                        ]
                    }

                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                ui: 'medium',
                hidden: true,
                itemId: 'con-configuration-view-validation-results-browse',
                items: [
                    {
                        ui: 'medium',
                        margin: '0 -16 0 -16',
                        itemId: 'rule-set-list',
                        title: Uni.I18n.translate('validationResults.validationRuleSets', 'MDC', 'Validation rule sets'),
                        xtype: 'mdc-rule-set-list',
                        router: me.router
                    },
                    {
                        ui: 'medium',
                        itemId: 'rule-set-version-list',
                        margin: '0 -16 0 -16',
                        title: Uni.I18n.translate('validationResults.validationRuleSetVersions', 'MDC', 'Validation rule set versions'),
                        xtype: 'mdc-rule-set-version-list',
                        router: me.router
                    },
                    {
                        ui: 'medium',
                        margin: '0 -16 0 -16',
                        itemId: 'rule-set-version-rule-list',
                        title: Uni.I18n.translate('validationResults.validationRuleSetVersionRules', 'MDC', 'Validation rules'),
                        xtype: 'rule-set-version-rule-list',
                        router: me.router
                    },
                    {
                        itemId: 'rule-set-version-rule-preview',
                        xtype: 'deviceDataValidationRulePreview',
                        router: me.router
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.mainView = Ext.ComponentQuery.query('#contentPanel')[0];
    },

    getStoreListeners: function () {
        return {
            beforeload: function() {
                this.mainView.setLoading();
            },
            load: this.onLoad
        };
    },

    onLoad: function (store, records, success) {
        var me = this,
            record = records ? records[0] : null,
            form = me.down('#frm-device-validation-results-ruleset'),
            dataViewValidateNowBtn = me.down('#btn-configuration-view-validate-now'),
            ruleSetPanel = me.down('#con-configuration-view-validation-results-browse'),
            ruleSetsGrid = me.down('#rule-set-list'),
            ruleSetVersionGrid = me.down('#rule-set-version-list'),
            ruleSetVersionRuleGrid = me.down('#rule-set-version-rule-list'),
            ruleSets;

        me.mainView.setLoading(false);

        if (success && record) {
            Ext.suspendLayouts();
            if (form) {
                form.loadRecord(record);
            }

            if (dataViewValidateNowBtn) {
                dataViewValidateNowBtn.setDisabled(!record.get('isActive') || record.get('allDataValidated'));
            }

            ruleSets = record.get('detailedRuleSets');
            if (ruleSets && ruleSets.length && ruleSetsGrid) {
                ruleSetsGrid.bindStore(record.detailedRuleSets());
                ruleSetsGrid.getSelectionModel().deselectAll();
                ruleSetVersionGrid.getSelectionModel().deselectAll();
                ruleSetVersionRuleGrid.getSelectionModel().deselectAll();
                ruleSetPanel.show();
                ruleSetsGrid.getSelectionModel().select(0);
            } else if (ruleSetPanel) {
                ruleSetPanel.hide();
            }
            Ext.resumeLayouts(true);
        }
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    }
});

