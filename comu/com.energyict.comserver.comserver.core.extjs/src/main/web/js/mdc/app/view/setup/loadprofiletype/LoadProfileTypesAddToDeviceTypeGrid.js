Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypesAddToDeviceTypeGrid',
    itemId: 'loadProfileTypesAddToDeviceTypeGrid',
    store: 'LoadProfileTypesOnDeviceType',
    height: 395,
    scroll: false,
    intervalStore: null,

    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    selType: 'checkboxmodel',
    selModel: {
        checkOnly: true,
        enableKeyNav: false,
        showHeaderCheckbox: false
    },
    requires: [
        'Uni.grid.column.Obis'
    ],
    initComponent: function () {
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },
            {
                header: 'Interval',
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = this.intervalStore.findRecord('id', value.id);
                    return intervalRecord.getData().name;
                },
                flex: 3
            }
        ];
        this.callParent();
    }
});