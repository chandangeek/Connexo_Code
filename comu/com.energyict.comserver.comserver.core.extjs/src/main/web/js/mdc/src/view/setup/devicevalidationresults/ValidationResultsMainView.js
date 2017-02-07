/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.mdc-device-validation-results-main-view',
    device: null,	
	requires: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsFilter',
		'Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset',
        'Mdc.view.setup.devicevalidationresults.ValidationResultsLoadProfileRegister'
	],
    router: null,
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
				layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
						ui: 'medium',
						items: [
							{
							xtype: 'deviceMenu',
							itemId: 'steps-Menu',
							device: me.device,
							toggleId: 'validationResultsLink'
						}
						]
                    }
                ]
            }
        ];
        me.content = [
            {
				xtype: 'tabpanel',
                ui: 'large',
                title: Uni.I18n.translate('validationResults.title', 'MDC', 'Validation results')
            },
            {
				xtype: 'tabpanel',
				itemId: 'tab-validation-results',	
				activeTab: -1,
                items: [
                    {
						ui: 'medium',
                        title: Uni.I18n.translate('validationResults.configurationView', 'MDC', 'Configuration view'),
                        itemId: 'validationResults-configuration',
						items: [
							{
								xtype: 'mdc-device-validation-results-filter',
								itemId: 'pnl-device-validation-results-filter',
                                store: 'Mdc.store.DeviceConfigurationResults',
                                duration: '3months'
							},
							{							
								xtype: 'mdc-device-validation-results-ruleset',
								itemId: 'pnl-device-validation-results-ruleset',
                                router: me.router
							}
						]
                    },
                    {
						ui: 'medium',
                        title: Uni.I18n.translate('validationResults.dataView', 'MDC', 'Data view'),
                        itemId: 'validationResults-data',
                        items: [
                            {
                                xtype: 'mdc-device-validation-results-filter',
                                itemId: 'validation-results-data-filter',
                                store: 'Mdc.store.DeviceValidationResults',
                                duration: '1years'
                            },
                            {
                                xtype: 'mdc-device-validation-results-load-profile-register',
                                itemId: 'pnl-device-validation-results-loadprofile-register',
                                router: me.router
                            }
                        ]
                    }
				]	                
              
            }
        ];
        me.callParent(arguments);
    }
});