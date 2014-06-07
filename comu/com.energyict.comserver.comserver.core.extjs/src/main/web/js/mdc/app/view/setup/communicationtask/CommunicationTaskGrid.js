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
                items:'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu'
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
                        itemId: 'communicationTasksCount',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'),
                        margin: '0 5',
                        hrefTarget: '',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/create'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

