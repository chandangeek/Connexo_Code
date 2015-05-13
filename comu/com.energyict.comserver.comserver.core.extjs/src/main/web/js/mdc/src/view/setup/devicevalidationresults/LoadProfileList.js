Ext.define('Mdc.view.setup.devicevalidationresults.LoadProfileList', {
    extend: 'Ext.grid.Panel',
    border: true,	
	alias: 'widget.mdc-load-profile-list',    
    store: 'Mdc.store.ValidationResultsLoadProfiles',
    requires: [
        'Mdc.store.ValidationResultsLoadProfiles'
    ],

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('device.dataValidation.loadProfile', 'MDC', 'Load profile'),
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
