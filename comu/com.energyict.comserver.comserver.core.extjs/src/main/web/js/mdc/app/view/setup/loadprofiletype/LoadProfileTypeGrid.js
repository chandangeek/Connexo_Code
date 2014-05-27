Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeGrid',
    itemId: 'loadProfileTypeGrid',
    height: 395,
    scroll: false,
    intervalStore: null,
    editActionName: null,
    deleteActionName: null,

    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    initComponent: function () {
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
                items: [

                    {
                        text: 'Edit',
                        action: this.editActionName
                    },
                    {
                        text: 'Remove',
                        action: this.deleteActionName
                    }

                ]
            }
        ];
        this.callParent();
    }
});

