Ext.define('Mdc.view.setup.register.RegisterMappingAddGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingAddGrid',
    overflowY: 'auto',
    itemId: 'registermappingaddgrid',
    selModel: {
        mode: 'MULTI'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterMappingsNotPartOfDeviceType'
    ],
    store: 'RegisterMappingsNotPartOfDeviceType',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 1
            },
            {
                header: 'Reading type',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                flex: 1
            },
            {
                header: 'Type',
                dataIndex: 'type',
                flex: 1
            }
        ];

        this.callParent();
    }
});
