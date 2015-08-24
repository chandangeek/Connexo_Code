Ext.define('Mdc.view.setup.executionlevels.ExecutionLevelGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.execution-level-grid',
    overflowY: 'auto',
    itemId: 'execution-level-grid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.executionlevels.ExecutionLevelActionMenu'
    ],
    store: 'ext-empty-store',
    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('executionLevel.executionlevel', 'MDC', 'Privilege'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('executionLevel.userroles', 'MDC', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(Ext.String.htmlEncode(userRole.name));
                    });
                    return resultArray.join('<br>');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                items: 'Mdc.view.setup.executionlevels.ExecutionLevelActionMenu'
            }

        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translatePlural('executionLevel.pagingtoolbartop.displayMsg', 0 ,'MDC', '{2} privileges'),
                emptyMsg: Uni.I18n.translate('executionLevel.pagingtoolbartop.emptyMsg', 'MDC', 'There are no privileges'),
                items: [
                    {
                        text: Uni.I18n.translate('executionLevels.addExecutionLevels', 'MDC', 'Add privileges'),
                        itemId: 'createExecutionLevel',
                        privileges: Mdc.privileges.DeviceType.admin,
                        xtype: 'button',
                        action: 'createExecutionLevel',
                        href: ''
                    }
                ]
            }
        ];

        this.callParent();
    }
})
;
