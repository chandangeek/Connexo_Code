Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communicationTaskGrid',
    itemId: 'communicationTaskGrid',
    deviceTypeId: null,
    deviceConfigurationId: null,
    store: 'CommunicationTaskConfigsOfDeviceConfiguration',
    scroll: false,
    requires: [
        'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('communicationtasks.task.name', 'MDC', 'Communication task'),
                dataIndex: 'comTask',
                renderer: function (value) {
                    return value.name;
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('communicationtasks.task.securityset', 'MDC', 'Security set'),
                dataIndex: 'securityPropertySet',
                renderer: function (value) {
                    return value.name;
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('communicationtasks.task.status', 'MDC', 'Status'),
                dataIndex: 'suspended',
                renderer: function (value) {
                    if (value === true) {
                        return Uni.I18n.translate('communicationtasks.task.inactive', 'MDC', 'Inactive');
                    }
                    return Uni.I18n.translate('communicationtasks.task.active', 'MDC', 'Active');
                },
                fixed: true,
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                items:'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'container',
                border: 0,
                margin: '0 0 5 0',
                align: 'left',
                dock: 'top',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'communicationTasksCount',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task configuration'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/add'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

