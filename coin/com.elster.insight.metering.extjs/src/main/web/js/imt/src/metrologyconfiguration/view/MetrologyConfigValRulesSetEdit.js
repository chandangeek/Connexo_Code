Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigValRulesSetEdit',
    itemId: 'metrologyConfigValRulesSetEdit',
    requires: ['Ext.ux.form.MultiSelect',
               'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
               'Imt.metrologyconfiguration.store.LinkedValidationRulesSet'],
    content: [
        {
            xtype: 'form',
            itemId: 'metrologyConfigValRulesSetEditForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    itemId: 'metrology-configuration-name',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('metrologyconfiguration.label.name', 'IMT', 'Metrology Configuration Name'),
                    allowBlank: true,
                    margin: '20 10 50 50',
                    readOnly: true,
                    maxLength: 75,
                    width: 600
                }, 
                {
                    xtype: 'textfield',
                    itemId: 'metrology-configuration-mcid',
                    name: 'mcid',
                    fieldLabel: Uni.I18n.translate('metrologyconfiguration.label.mcid', 'IMT', 'mcid'),
                    allowBlank: true,
                    hidden: true,
                    readOnly: true,
                    maxLength: 75,
                    width: 600
                }, 
                {
                	 xtype: 'panel',
	                 required: true,
	                 border: '5 5 5 5',
	                 height: 200,
	                 layout: 'hbox',
	                 items: [
	                    {
                            xtype: 'panel',
                            title: Uni.I18n.translate('metrologyconfiguration.label.linkedvalrulesets', 'IMT', 'Linked Validation Rule sets'),
                            required: true,
                            border: '5 5 5 5',
                            frame: true,
                            height: 200,
                            width: 300,
                            border: 10,
                            autoScroll: true,
                            items: [
        		                {
                                    xtype: 'multiselect',                            
                                    itemId: 'metrology-config-linked-val-rules-set',
                                    name: 'linkedValidationRulesSets',
                                    width: 235,
                                    store: 'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
                                    queryMode: 'local',
                                    valueField: 'id',
                                    displayField: 'name',
                                    listConfig: { border: false }
        		                }
        		            ]
                        },
		                {
		                    xtype: 'panel',
		                    title: Uni.I18n.translate('general.label.actions', 'IMT', 'Actions'),
		                    required: true,
			                border: '5 5 5 5',
	                        margin: '0 10 0 10',
		                    frame: true,
		                    height: 200,
		                    width: 150,
		                    border: 10,
		                    layout: 'vbox',
		                    items: [
		                        {
		                            text: Uni.I18n.translate('metrologyconfiguration.button.remove.valrulesets', 'IMT', 'Remove >>>'),
		                            xtype: 'button',
		                            width: 100,
		                            margin: '0 5 5 5',
		                            ui: 'action',
		                            action: 'removeRulesSet',
		                            itemId: 'removeRulesSetButton',
		                        },
		                        {
		                            text: Uni.I18n.translate('metrologyconfiguration.button.add.valrulesets', 'IMT', '<<< Add'),
		                            xtype: 'button',
		                            width: 100,
		                            margin: '0 5 10 5',
		                            ui: 'action',
		                            action: 'addRulesSet',
		                            itemId: 'addRulesSetButton',
		                        },
		                        {
		                            text: Uni.I18n.translate('general.button.save', 'IMT', 'Save'),
		                            xtype: 'button',
		                            width: 100,
		                            margin: '10 5 5 5',
		                            ui: 'action',
		                            action: 'saveModel',
		                            itemId: 'createEditButton'
		                        },
		                        {
		                            text: Uni.I18n.translate('general.button.cancel', 'IMT', 'Cancel'),
		                            xtype: 'button',
		                            width: 100,
		                            margin: '0 5 5 5',
		                            ui: 'action',
		                            action: 'cancelButton',
		                            itemId: 'cancelButton'
		                        }
		                    ]
		                },
		                {
                            xtype: 'panel',
                            title: Uni.I18n.translate('metrologyconfiguration.label.linkablevalrulesets', 'IMT', 'Linkable Validation Rule sets'),
                            required: true,
                            border: '5 5 5 5',
                            frame: true,
                            height: 200,
                            width: 300,
                            border: 10,
                            autoScroll: true,
                            items: [
        		                {
                                    xtype: 'multiselect',
                                    itemId: 'metrology-config-linkable-val-rules-set',
                                    name: 'linkableValidationRulesSets',
                                    width: 235,
                                    store: 'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
                                    queryMode: 'local',
                                    valueField: 'id',
                                    displayField: 'name',
                                    listConfig: { border: false }
        		                }
        		            ]
                        }
		            ]
                },

            ]
        }
    ],

});
