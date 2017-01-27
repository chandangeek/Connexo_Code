Ext.define('Dal.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dal.privileges.Alarm',
        'Dal.view.ActionMenu'
    ],
    alias: 'widget.alarms-grid',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'alarms-grid-title',
                header: Uni.I18n.translate('general.title.alarm', 'DAL', 'Alarm'),
                dataIndex: 'title',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({alarmId: record.getId()});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                itemId: 'alarms-grid-priority',
                header: Uni.I18n.translate('general.priority', 'DAL', 'Priority'),
                dataIndex: 'priority',
                flex: 1
            },
            {
                itemId: 'alarms-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'DAL', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'alarms-grid-status',
                header: Uni.I18n.translate('general.status', 'DAL', 'Status'),
                dataIndex: 'statusName',
                flex: 1
            },
            {
                itemId: 'alarms-grid-cleared',
                header: Uni.I18n.translate('general.cleared', 'DAL', 'Cleared'),
                dataIndex: 'cleared',
                flex: 1
            },
            {
                itemId: 'alarms-grid-workgroup',
                header: Uni.I18n.translate('general.workgroup', 'DAL', 'Workgroup'),
                dataIndex: 'workgroup',
                flex: 1
            },
            {
                itemId: 'alarms-grid-user',
                header: Uni.I18n.translate('general.user', 'DAL', 'User'),
                dataIndex: 'user',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                privileges: !!Dal.privileges.Alarm.adminDevice,
                menu: {
                    xtype: 'alarms-action-menu',
                    itemId: 'alarm-overview-action-menu',
                    router: me.router
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.alarms.pagingtoolbartop.displayMsg', 'DAL', '{0} - {1} of {2} alarms'),
                displayMoreMsg: Uni.I18n.translate('workspace.alarms.pagingtoolbartop.displayMoreMsg', 'DAL', '{0} - {1} of more than {2} alarms'),
                emptyMsg: Uni.I18n.translate('workspace.alarms.pagingtoolbartop.emptyMsg', 'DAL', 'There are no alarms to display')
                ,
             items: [
                 {
                     xtype: 'button',
                     itemId: 'alarms-bulk-action',
                     text: Uni.I18n.translate('general.title.bulkActions', 'DAL', 'Bulk action'),
                     privileges: Dal.privileges.Alarm.adminDevice,
                     action: 'alarmsBulkAction',
                     handler: function () {
                         me.router.getRoute(me.router.currentRoute + '/bulkaction').forward(me.router.arguments, Uni.util.QueryString.getQueryStringValues(false));
                     }
                 }
             ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.alarms.pagingtoolbarbottom.itemsPerPage', 'DAL', 'Alarms per page')
            }
        ];
        me.callParent(arguments);
    }
});