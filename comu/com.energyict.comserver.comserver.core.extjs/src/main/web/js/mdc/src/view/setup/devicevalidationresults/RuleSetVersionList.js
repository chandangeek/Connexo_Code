Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetVersionList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.ruleSetVersionList',

    store: 'Mdc.store.ValidationResultsVersions',

    requires: [
        'Mdc.store.ValidationResultsVersions'
    ],

    columns: {
        items: [
          { 
				header: Uni.I18n.translate('validationResults.version', 'MDC', 'Version'), 
				dataIndex: 'versionName', 
				flex: 0.7, 
				sortable: false, 
				fixed: true,
                renderer: function (value, metaData, record) {                 					
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.getId() + '">' + value + '</a>';
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
