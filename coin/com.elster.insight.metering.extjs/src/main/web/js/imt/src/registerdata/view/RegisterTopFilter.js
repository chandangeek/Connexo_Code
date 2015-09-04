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
            defaultFromDate: moment().startOf('day').subtract(1,'years').toDate(),
            defaultDuration: '1months',
            text: Uni.I18n.translate('communications.widget.topfilter.startedDate', 'IMT', 'Start date'),
            durationStore: me.filterDefault.durationStore,
            loadStore: false
        }];

        me.callParent(arguments);
    }

});