Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsLoadProfileRegister', {
    extend: 'Uni.view.container.ContentContainer',	
   
    alias: 'widget.deviceValidationResultsLoadProfileRegister',
    itemId: 'deviceValidationResultsLoadProfileRegister',
	ui: 'medium',
	layout: {
		type: 'vbox',
		align: 'stretch'
	},
	requires :[
		'Mdc.view.setup.devicevalidationresults.RegisterList',
		'Mdc.view.setup.devicevalidationresults.LoadProfileList'

	],
	margin: '0 0 0 -16',
	initComponent: function () {
		this.content = [
			{
				xtype: 'form',
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				defaults: {
					labelWidth: 250,
					labelAlign: 'left'
				},

				items: [
					{
					 xtype: 'displayfield',
					 itemId: 'data-view-data-validated',
					 fieldLabel: Uni.I18n.translate('validationResults.dataValidated', 'MDC', 'Data validated'),
					 name: 'dataValidated'

					 },
					 {
					 xtype: 'displayfield',
					 itemId: 'data-view-validation-results',
					 fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
					 name: 'validationResultsCount'
					 }
				]

			},
			{
				xtype: 'container',
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				itemId: 'data-view-validation-results-browse',
				items: [
					{
						 ui: 'large',
						 itemId: 'validation-result-load-profile-list',
						 title: Uni.I18n.translate('device.dataValidation.loadProfiles', 'MDC', 'Load profiles'),
						 xtype: 'loadProfileList'
					 },
					 {
						 ui: 'large',
						 itemId: 'validation-result-register-list',
						 title: Uni.I18n.translate('device.dataValidation.registers', 'MDC', 'Registers'),
						 xtype: 'registerList'
					 }

				]
			}



		];
		this.callParent(arguments);

	}

});

