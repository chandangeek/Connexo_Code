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
        defaults: {
            flex: 1
        },
        items: [
            {
                header: 'Name',
                dataIndex: 'name'
            },
            {
                header: 'Authentication level',
                dataIndex: 'authenticationLevel',
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                header: 'Encryption level',
                dataIndex: 'encryptionLevel',
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

