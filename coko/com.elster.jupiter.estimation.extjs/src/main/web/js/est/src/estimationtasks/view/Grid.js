Ext.define('Est.estimationtasks.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Est.estimationtasks.view.ActionMenu'
    ],

    alias: 'widget.estimationtasks-grid',
    store: 'Est.estimationtasks.store.EstimationTasks',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('estimationtasks.general.estimationTasksLabel', 'EST', 'Estimation Task'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationtasks.general.status', 'EST', 'Status'),
                dataIndex: 'status_formatted',
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationtasks.general.nextRun', 'EST', 'Next run'),
                dataIndex: 'nextRun_formatted_short',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'estimationtasks-action-menu',
                    itemId: 'estimationtasks-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.displayMsg', 'EST', '{0} - {1} of {2} estimation tasks'),
                displayMoreMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.displayMoreMsg', 'EST', '{0} - {1} of more than {2} estimation tasks'),
                emptyMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.emptyMsg', 'EST', 'There are no estimation tasks to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('estimationtasks.general.addEstimationTask', 'EST', 'Add estimation task'),
                        privileges: Est.privileges.EstimationConfiguration.administrateTask,
                        href: '#/administration/estimationtasks/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationtasks.pagingtoolbarbottom.itemsPerPage', 'EST', 'Estimation tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
