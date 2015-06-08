Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,	
    alias: 'widget.mdc-rule-set-list',	
    store: 'Mdc.store.ValidationResultsRuleSets',
    requires: [
        'Mdc.store.ValidationResultsRuleSets'
    ],

	router: null,
    columns: {
        items: [
            { 
				header: Uni.I18n.translate('validationResults.validationRuleSet', 'MDC', 'Validation rule set'), 
				dataIndex: 'name', 
				flex: 0.7, 
				sortable: false, 
				fixed: true,
                renderer: function (value, metaData, record) {					                    
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },            
            { 
				header: Uni.I18n.translate('validationResults.result', 'MDC', 'Result'), 
				dataIndex: 'total', 
				sortable: false, 
				fixed: true


            }
        ]
    }


});
