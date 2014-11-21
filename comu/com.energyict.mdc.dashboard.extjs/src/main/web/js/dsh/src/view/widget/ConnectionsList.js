Ext.define('Dsh.view.widget.ConnectionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
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
                flex: 1,
                renderer: function (val) {
                    return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                }
            },
            {
                itemId: 'connectionMethod',
                text: Uni.I18n.translate('connection.widget.details.connectionMethod', 'DSH', 'Connection method'),
                dataIndex: 'connectionMethod',
                flex: 1,
                renderer: function (val) {
                    return val ? val.name : ''
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val ? val.displayValue : ''
                }
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                dataIndex: 'latestStatus',
                flex: 1,
                renderer: function (val) {
                    return val ? val.displayValue : ''
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                name: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val ? val.displayValue : ''

                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (val) {
                    var template = '';
                    if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                        template += '<tpl><img src="/apps/dsh/resources/images/widget/running.png" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</span></tpl>';
                        template += '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" class="ct-result ct-failure" style="position: relative; left: 30px"><span style="position: relative; top: -3px; left: 34px">' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</span></tpl>';
                        template += '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" class="ct-result ct-incomplete" style="position: relative; left: 56px"><span  style="position: relative; top: -3px; left: 60px">' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</span></tpl>';
                    }
                    return template;
                },
                header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                xtype: 'datecolumn',
                format: 'd/m/Y h:i:s',
                flex: 1
            },
            {
                itemId: 'connectionsGridActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {

                }
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
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate('connection.widget.details.itemsPerPage', 'DSH', 'Connections per page')
        }
    ],

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'img.ct-result',
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
                        (tip.triggerElement.className.search('ct-success') !== -1) && (res = success);
                        (tip.triggerElement.className.search('ct-failure') !== -1) && (res = failed);
                        (tip.triggerElement.className.search('ct-incomplete') !== -1) && (res = notCompleted);
                        tip.update(res);
                    }
                }
            }),
            ResultTip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'td.x-grid-cell-headerId-latestResult',
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    show: function () {
                        var rowEl = Ext.get(ResultTip.triggerElement).up('tr'),
                            latestResult = view.getRecord(rowEl).get('latestResult');
                        if (latestResult.retries) {
                            ResultTip.update(latestResult.retries + ' ' + Uni.I18n.translate('connection.widget.details.retries', 'DSH', 'retries'));
                        } else {
                            ResultTip.hide()
                        }
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
