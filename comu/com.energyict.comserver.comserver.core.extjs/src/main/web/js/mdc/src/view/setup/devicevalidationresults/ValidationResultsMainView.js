Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.mdc-device-validation-results-main-view',
    device: null,	
	requires: [
		'Mdc.view.setup.devicevalidationresults.SideFilter',
		'Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset',
        'Mdc.view.setup.devicevalidationresults.ValidationResultsLoadProfileRegister',
		'Mdc.store.ValidationResultsDurations'
	],  
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
                    },
                    {
					      xtype: 'mdc-device-validation-results-side-filter',
						  itemId: 'device-validation-result-side-filter'
                    }

                ]
            }
        ];
        me.content = [
            {
				xtype: 'tabpanel',                
                ui: 'large',
                title: Uni.I18n.translate('validationResults.title', 'MDC', 'Validation results'),
				itemId: 'tab-validation-results',	
				activeTab: -1,
                items: [
                    {
						ui: 'medium',
                        title: Uni.I18n.translate('validationResults.configurationView', 'MDC', 'Configuration view'),
                        itemId: 'validationResults-configuration',
						items: [
							{
								xtype: 'filter-top-panel',
								itemId: 'pnl-device-validation-results-filter',
								emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
							},
							{							
								xtype: 'mdc-device-validation-results-ruleset',
								itemId: 'pnl-device-validation-results-ruleset'
							}
						]
                    },
                    {
						ui: 'medium',
                        title: Uni.I18n.translate('validationResults.dataView', 'MDC', 'Data view'),
                        itemId: 'validationResults-data',
                        items: [
                            {
                                xtype: 'filter-top-panel',
                                itemId: 'validation-results-data-filter',
                                emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                            },
                            {
                                xtype: 'mdc-device-validation-results-load-profile-register',
                                itemId: 'pnl-device-validation-results-loadprofile-register'
                            }
                        ]
                    }
				]	                
              
            }
        ];
        me.callParent(arguments);
    }
});