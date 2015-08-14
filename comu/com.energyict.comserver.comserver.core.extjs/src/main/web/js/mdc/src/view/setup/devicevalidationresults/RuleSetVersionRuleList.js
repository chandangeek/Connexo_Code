Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.rule-set-version-rule-list',
    store: 'Mdc.store.ValidationResultsRules',
    requires: [
        'Mdc.store.ValidationResultsRules'
    ],
	router: null,
	
    columns: {
        items: [
            { 
				header: Uni.I18n.translate('validationResults.validationRule', 'MDC', 'Validation rule'), 
				dataIndex: 'name', 
				flex: 0.4, 
				sortable: false, 
				fixed: true,
                renderer: function (value, metaData, record) {			
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            { 
				header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
				dataIndex: 'active', 
				flex: 0.3, 
				align: 'left', 
				sortable: false, 
				fixed: true
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
