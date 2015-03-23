Ext.define('Cfg.view.validationtask.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tasks-grid',
    store: 'Cfg.store.ValidationTasks',
    router: null,	
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validationTasks.general.name', 'CFG', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {				
                    var url = me.router.getRoute('administration/validationtasks/datavalidationtask').buildUrl({taskId: record.get('id')});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('validationTasks.general.status', 'CFG', 'Status'),
                dataIndex: 'lastExportOccurence',
                renderer: function (value) {
                    var result;
                    if (value && value.statusDate && value.statusDate != 0) {
                        result = value.statusPrefix + ' ' + Uni.DateTime.formatDateTimeShort(new Date(value.statusDate));
                    } else if (value) {
                        result = value.statusPrefix
                    } else {
                        result = Uni.I18n.translate('validationTasks.general.notPerformed', 'CFG', 'Not performed yet');
                    }
                    return result;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('validationTasks.general.nextRun', 'CFG', 'Next run'),
                dataIndex: 'nextRun',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : Uni.I18n.translate('validationTasks.general.notScheduled', 'CFG', 'Not scheduled');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'tasks-action-menu',
                    itemId: 'tasks-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} validation tasks'),
                displayMoreMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} validation tasks'),
                emptyMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.emptyMsg', 'CFG', 'There are no validation tasks to display'),
                items: [
                    '->',
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validationTasks.general.addDataValidationTask', 'CFG', 'Add validation task'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
                        href: '#/administration/validationtasks/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validationTasks.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Validation tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
