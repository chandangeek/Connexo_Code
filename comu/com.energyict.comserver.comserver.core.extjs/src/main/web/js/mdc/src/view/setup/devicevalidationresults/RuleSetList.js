Ext.define('Mdc.view.setup.devicevalidationresults.RuleSetList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.ruleSetList',

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
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
                }
            },            
            { 
				header: Uni.I18n.translate('validationResults.result', 'MDC', 'Result'), 
				dataIndex: 'total', 
				sortable: false, 
				fixed: true,
				renderer: function (value, meta, record) {					
					var me = this,
						href = me.router.getRoute('devices/device/validationresultsdata').buildUrl({mRID: record.get('mRID'), ruleSetId: record.get('id')}, me.router.queryParams );
					return '<a href="' + href + '">' + value + '</a>'
				}

            }
        ]
    }


});
