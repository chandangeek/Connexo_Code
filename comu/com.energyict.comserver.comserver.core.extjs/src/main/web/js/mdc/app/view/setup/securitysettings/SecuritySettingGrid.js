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
    columns: {
        items: [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 0.2
            },
            {
                header: 'Authentication level',
                dataIndex: 'authenticationLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                header: 'Encryption level',
                dataIndex: 'encryptionLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
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
        ]
    }
});

