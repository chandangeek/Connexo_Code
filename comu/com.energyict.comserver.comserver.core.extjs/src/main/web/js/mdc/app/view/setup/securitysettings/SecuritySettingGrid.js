Ext.define('Mdc.view.setup.securitysettings.SecuritySettingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.securitySettingGrid',
    itemId: 'securitySettingGrid',
    deviceTypeId: null,
    deviceConfigId: null,
    store: 'Mdc.store.SecuritySettingsOfDeviceConfiguration',
    height: 395,
    scroll: false,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: 'Authentication level',
                dataIndex: 'authenticationLevel',
                renderer: function (value) {
                    return value.name;
                },
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: 'Encryption level',
                dataIndex: 'encryptionLevel',
                renderer: function (value) {
                    return value.name;
                },
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                items: [

                    {
                        text: 'Edit',
                        action: 'editsecuritysetting'
                    },
                    {
                        text: 'Remove',
                        action: 'deletesecuritysetting'
                    }

                ]
            }
        ];
        this.callParent();
    }
});

