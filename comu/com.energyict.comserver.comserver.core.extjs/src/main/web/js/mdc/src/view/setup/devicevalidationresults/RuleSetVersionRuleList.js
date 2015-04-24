Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.ruleSetVersionRuleList',

    store: 'Mdc.store.ValidationResultsRules',

    requires: [
        'Mdc.store.ValidationResultsRules'
    ],

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('validationResults.validationRule', 'MDC', 'Validation rule'), 
				dataIndex: 'name', 
				flex: 0.4, 
				sortable: false, 
				fixed: true/*,
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description').replace(/(?:\r\n|\r|\n)/g, '<br />') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>'
                }*/
            },
            { 
				header: Uni.I18n.translate('validationResults.status', 'MDC', 'Status'), 
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
				fixed: true/*,
                renderer: function (value, b, record) {
                    var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
                    return numberOfActiveRules;
                }*/
            }
        ]
    }


});
