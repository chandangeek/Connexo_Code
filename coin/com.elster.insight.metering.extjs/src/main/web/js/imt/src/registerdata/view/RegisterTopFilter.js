Ext.define('Imt.registerdata.view.RegisterTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'imt-registerdata-topfilter',
    store: 'Imt.registerdata.store.RegisterData',
    filterDefault: {},
    
    initComponent: function() {
        var me = this;

        this.filters = [{
	            type: 'duration',
	            dataIndex: 'interval',
	            dataIndexFrom: 'intervalStart',
	            dataIndexTo: 'intervalEnd',
	 //           defaultFromDate: moment().startOf('day').subtract(1,'years').toDate(),
	            defaultFromDate: moment().startOf('day').subtract(1,'months').toDate(),
	            defaultDuration: '1months',
	            text: Uni.I18n.translate('general.label.startdate', 'IMT', 'Start date'),
	            durationStore: me.filterDefault.durationStore,
	            loadStore: false
	        },
	        {
	            type: 'checkbox',
	            dataIndex: 'suspect',
	            layout: 'hbox',
	            defaults: {margin: '0 10 0 0'},
	            emptyText: Uni.I18n.translate('communications.widget.topfilter.validationResult', 'IMT', 'Validation result'),
	            options: [
	                {
	                    display: Uni.I18n.translate('validationStatus.suspect', 'IMT', 'Suspect'),
	                    value: 'suspect'
	                },
	                {
	                    display: Uni.I18n.translate('validationStatus.ok', 'IMT', 'Not suspect'),
	                    value: 'nonSuspect'
	                }
	            ]
	        }
        ];

        me.callParent(arguments);
    }

});