Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communicationSchedulesGrid',
    overflowY: 'auto',
    itemId: 'communicationSchedulesGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationSchedules'
    ],
    store: 'CommunicationSchedules',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('communicationschedule.status', 'MDC', 'Status'),
                dataIndex: 'schedulingStatus',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.1
            },
            {
                header: Uni.I18n.translate('communicationschedule.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                dataIndex: 'temporalExpression',
                renderer: function (value, metadata) {
                    switch (value.every.timeUnit) {
                        case 'months':
                            return Uni.I18n.translate('general.monthly', 'MDC', 'Monthly');
                        case 'weeks':
                            return Uni.I18n.translate('general.weekly', 'MDC', 'Weekly');
                        case 'days':
                            return Uni.I18n.translate('general.daily', 'MDC', 'Daily');
                        case 'hours':
                            return Uni.I18n.translate('general.hourly', 'MDC', 'Hourly');
                        case 'minutes':
                            return Uni.I18n.translate('general.everyFewMinutes', 'MDC', 'Every few minutes');
                    }
                    return value.every.timeUnit;
                },
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                dataIndex: 'plannedDate',
                sortable: false,
                hideable: false,
                renderer: function (value) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    } else {
                        return '';
                    }
                },
                fixed: true,
                flex: 0.4
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                sortable: false,
                hideable: false,
                items: [
                    {
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('editItem', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('deleteItem', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    }
                                ]
                            });
                            menu.showAt(e.getXY());
                        }
                    }
                ]
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communication schedules'),
                displayMoreMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication schedules'),
                emptyMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communication schedules to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('communicationschedule.createCommunicationSchedule', 'MDC', 'Create communication schedule'),
                        itemId: 'createCommunicationSchedule',
                        xtype: 'button',
                        action: 'createCommunicationSchedule'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('communicationschedule.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communication schedules per page')
            }
        ];

        this.callParent();
    }
});
