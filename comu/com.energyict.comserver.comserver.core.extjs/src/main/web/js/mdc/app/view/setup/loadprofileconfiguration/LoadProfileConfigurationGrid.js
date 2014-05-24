Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationGrid',
    itemId: 'loadProfileConfigurationGrid',
    store: 'LoadProfileConfigurationsOnDeviceConfiguration',
    scroll: false,
    intervalStore: null,
    deviceTypeId: null,
    deviceConfigurationId: null,
    height: 395,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },

    initComponent: function () {
        this.columns = [
            {
                header: 'Load profile type',
                dataIndex: 'id',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}">{3}</a>', this.deviceTypeId, this.deviceConfigurationId, value, this.store.findRecord('id', value).getData().name);
                }
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: 'Interval',
                dataIndex: 'timeDuration',
                sortable: false,
                hideable: false,
                fixed: true,
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
