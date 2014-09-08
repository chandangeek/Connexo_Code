Ext.define('Dsh.view.widget.ConnectionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dsh.view.widget.ActionMenu'
    ],
    itemId: 'connectionslist',
    store: 'Dsh.store.ConnectionTasks',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Device',
                text: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 2,
                renderer: function (val) {
                    return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                dataIndex: 'latestStatus',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue
                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (val) {
                    var success = val.numberOfSuccessfulTasks ? '<tpl><span class="fa fa-check fa-lg" style="color: green; position: relative; vertical-align: 0% !important;"></span><span style="position: relative; left: 4px">' + val.numberOfSuccessfulTasks + '</span></tpl>' : '',
                        failed = val.numberOfFailedTasks ? '<tpl><span class="fa fa-times fa-lg" style="color: red; position: relative; left: 26px; vertical-align: 0% !important;"></span><span style="position: relative; left: 30px">' + val.numberOfFailedTasks + '</span></tpl>' : '',
                        notCompleted = val.numberOfIncompleteTasks ? '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; position: relative; left: 52px; vertical-align: 0% !important"></span><span  style="position: relative; left: 56px">' + val.numberOfIncompleteTasks + '</span></tpl>' : ''
                        ;
                    return success + failed + notCompleted
                },
                header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'endDateTime',
                text: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                dataIndex: 'endDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
//                ,
//                items: 'Dsh.view.widget.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Dsh.store.ConnectionTasks',
            displayMsg: Uni.I18n.translate('connection.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} connections'),
            displayMoreMsg: Uni.I18n.translate('connection.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} connections'),
            emptyMsg: Uni.I18n.translate('connection.widget.details.emptyMsg', 'DSH', 'There are no connections to display')
//            ,
//            items: [
//                '->',
//                {
//                    xtype: 'button',
//                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Choose columns'),
//                    action: 'choosecolumnsaction'
//                },
//                {
//                    xtype: 'button',
//                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Bulk action'),
//                    action: 'bulkchangesissues'
//                }
//            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('connection.widget.details.itemsPerPage', 'DSH', 'Connections per page')
        }
    ],

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'span.fa',
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    beforeshow: function updateTipBody(tip) {
                        var res,
                            rowEl = Ext.get(tip.triggerElement).up('tr'),
                            taskCount = view.getRecord(rowEl).get('taskCount'),
                            failed = taskCount.numberOfFailedTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksFailed', 'DSH', 'communication tasks failed'),
                            success = taskCount.numberOfSuccessfulTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksSuccessful', 'DSH', 'communication tasks successful'),
                            notCompleted = taskCount.numberOfIncompleteTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksNotCompleted', 'DSH', 'communication tasks not completed');
                        (tip.triggerElement.className.search('fa-ban') !== -1) && (res = notCompleted);
                        (tip.triggerElement.className.search('fa-check') !== -1) && (res = success);
                        (tip.triggerElement.className.search('fa-time') !== -1) && (res = failed);
                        tip.update(res);
                    }
                }
            });

    },

    initComponent: function () {
        var me = this;
        me.on('afterrender', me.addTooltip);
        me.callParent(arguments);
    }
});
