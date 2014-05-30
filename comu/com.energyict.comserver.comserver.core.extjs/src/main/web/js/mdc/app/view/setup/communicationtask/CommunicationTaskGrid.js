Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communicationTaskGrid',
    itemId: 'communicationTaskGrid',
    deviceTypeId: null,
    deviceConfigId: null,
    store: 'CommunicationTaskConfigsOfDeviceConfiguration',
    scroll: false,
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
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        action: 'editcommunicationtask'
                    },
                    {
                        text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                        action: 'activatecommunicationtask'
                    },
                    {
                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        action: 'removecommunicationtask'
                    }

                ]
            }
        ];
        this.callParent(arguments);
    }
});

