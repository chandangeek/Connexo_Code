Ext.define('Mdc.view.setup.devicevalidationresults.RegisterList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.registerList',

    store: 'Mdc.store.ValidationResultsRegisters',

    requires: [
        'Mdc.store.ValidationResultsRegisters'
    ],

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('device.dataValidation.register', 'MDC', 'Register'),
				dataIndex: 'name', 
				flex: 0.7, 
				sortable: false, 
				fixed: true/*,

                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description').replace(/(?:\r\n|\r|\n)/g, '<br />') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>'
                }*/
            },            
            { 
				header: Uni.I18n.translate('validationResults.result', 'MDC', 'Result'), 
				dataIndex: 'total', 
				//flex: 0.1, 
				//align: 'left', 
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
