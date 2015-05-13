Ext.define('Mdc.view.setup.devicevalidationresults.RegisterList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.mdc-register-list',
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
