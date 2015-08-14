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
        'Yfn.privileges.Yellowfin',
        'Dsh.view.widget.ConnectionActionMenu'
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
                text: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return  (Mdc.privileges.Device.canView() || Mdc.privileges.Device.canAdministrateDeviceData())
                        ? '<a href="#/devices/' + val.id + '">' + Ext.String.htmlEncode(val.name) + '</a>' : Ext.String.htmlEncode(val.name)
                }
            },
            {
                itemId: 'connectionMethod',
                text: Uni.I18n.translate('general.connectionMethod', 'DSH', 'Connection method'),
                dataIndex: 'connectionMethod',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.name) : ''
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('general.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.displayValue) : ''
                }
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('general.latestStatus', 'DSH', 'Latest status'),
                dataIndex: 'latestStatus',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.displayValue) : ''
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('general.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                name: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val.displayValue) : ''

                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (val,metaData) {
                    metaData.tdCls = 'communication-tasks-status';
                    var template = '';
                    if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                        template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                        template += '<tpl><span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                        template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                    }
                    return template;
                },
                header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 1
            },
            {
                itemId: 'connectionsActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'connection-action-menu',
                    itemId: 'connectionsActionMenu'
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
            displayMsg: Uni.I18n.translate('connection.widget.details.displayMsg', 'DSH', '{0} - {1} of {2} connections'),
            displayMoreMsg: Uni.I18n.translate('connection.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} connections'),
            emptyMsg: Uni.I18n.translate('connection.widget.details.emptyMsg', 'DSH', 'There are no connections to display'),
            items:[
                {
                    xtype:'button',
                    itemId:'generate-report',
                    privileges: Yfn.privileges.Yellowfin.view,
                    text:Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                },
                {
                    xtype:'button',
                    itemId:'btn-connections-bulk-action',
                    privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                    text: Uni.I18n.translate('general.bulkAction', 'DSH', 'Bulk action')
                }
            ]
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
