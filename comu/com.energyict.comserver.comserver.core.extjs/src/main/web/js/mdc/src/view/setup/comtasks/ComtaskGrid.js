Ext.define('Mdc.view.setup.comtasks.ComtaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskGrid',
    store: 'Mdc.store.CommunicationTasks',
    forceFit: true,
    autoScroll: false,
    enableColumnHide: false,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                header: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.communicationAdministration'),
                items: 'Mdc.view.setup.comtasks.ComtaskActionMenu',
                align: 'left'
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            store: 'Mdc.store.CommunicationTasks',
            displayMsg: Uni.I18n.translate('comtask.display.msg', 'MDC', '{0} - {1} of {2} communication tasks'),
            displayMoreMsg: Uni.I18n.translate('comtask.display.more.msg', 'MDC', '{0} - {1} of more than {2} communication tasks'),
            emptyMsg: Uni.I18n.translate('comtask.empty.msg', 'MDC', '0 communication tasks'),
            dock: 'top',
            border: false,
            items: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'),
                    hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.communicationAdministration'),
                    action: 'createcommunicationtasks',
                    href: '#/administration/communicationtasks/add'
                }
            ]

        },
        {
            xtype: 'pagingtoolbarbottom',
            itemsPerPageMsg: Uni.I18n.translate('comtask.items.per.page.msg', 'MDC', 'Communication tasks per page'),
            store: 'Mdc.store.CommunicationTasks',
            dock: 'bottom'
        }
    ]
});