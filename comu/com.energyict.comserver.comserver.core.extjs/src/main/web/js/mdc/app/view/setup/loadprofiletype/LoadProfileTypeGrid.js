Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeGrid',
    itemId: 'loadProfileTypeGrid',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu'
    ],
    height: 395,
    scroll: false,
    intervalStore: null,
    editActionName: null,
    deleteActionName: null,

    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 3
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                flex: 3
            },
            {
                header: 'Interval',
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = this.intervalStore.findRecord('id', value.id);
                    return intervalRecord.getData().name;
                },
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu'
            }
        ];
        this.callParent();
    }
});

