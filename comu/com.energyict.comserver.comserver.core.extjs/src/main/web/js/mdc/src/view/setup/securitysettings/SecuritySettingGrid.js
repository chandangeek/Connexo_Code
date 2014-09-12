Ext.define('Mdc.view.setup.securitysettings.SecuritySettingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.securitySettingGrid',
    itemId: 'securitySettingGrid',
    requires: [
        'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
    ],
    deviceTypeId: null,
    deviceConfigId: null,
    store: 'Mdc.store.SecuritySettingsOfDeviceConfiguration',
    scroll: false,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },

    initComponent: function () {
        var me = this;

        me.columns = [
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
                items: 'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                border: 0,
                align: 'left',
                dock: 'top',
                items: [
                    {
                        xtype: 'container',
                        itemId: 'securityCount',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: 'Add security setting',
                        action: 'addsecurityaction',
                        margin: '0 5',
                        hrefTarget: '',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/create'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

