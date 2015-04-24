Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceValidationResultsMainView',
    itemId: 'deviceValidationResultsMainView',

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
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'validationResultsLink'
                    },
                    {
                        xtype: 'deviceValidationResultsSideFilter'
                    }

                ]
            }
        ];
        me.content = [
            {
				xtype: 'tabpanel',                
                ui: 'large',
                title: Uni.I18n.translate('validationResults.title', 'MDC', 'Validation results'),
				itemId: 'validationResultsTabPanel',	
				activeTab: -1,
                items: [
                    {
                        title: Uni.I18n.translate('validationResults.configurationView', 'MDC', 'Configuration view'),
                        itemId: 'validationResults-configuration',
						items: [
							{
								xtype: 'filter-top-panel',
								itemId: 'devicevalidationresultsfilterpanel',
								emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
							},
							{
								xtype: 'deviceValidationResultsRuleset',
								itemId: 'devicevalidationresultsrulesetpanel'
							}
						]
                    },
                    {
                        title: Uni.I18n.translate('validationResults.dataView', 'MDC', 'Data view'),
                        itemId: 'validationResults-data',
                        items: [
                            {
                                xtype: 'filter-top-panel',
                                itemId: 'devicevalidationresultsdatafilterpanel',
                                emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                            },
                            {
                                xtype: 'deviceValidationResultsLoadProfileRegister',
                                itemId: 'deviceValidationResultsLoadProfileRegisterpanel'
                            }
                        ]
                    }
				]	                
              
            }
        ];
        me.callParent(arguments);
    }
});