Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communicationSchedulesGrid',
    overflowY: 'auto',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationSchedules',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
    ],
    store: 'Mdc.store.CommunicationSchedules',
    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                dataIndex: 'temporalExpression',
                renderer: function (value, metadata) {
                    metadata.tdAttr = 'data-qtip="' + Mdc.util.ScheduleToStringConverter.convert(value) + '"';
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
                flex: 1
            },
            {
                header: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                dataIndex: 'plannedDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.CommunicationSchedule.admin,
                items: 'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
            }

        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} shared communication schedules'),
                displayMoreMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} shared communication schedules'),
                emptyMsg: Uni.I18n.translate('communicationschedule.pagingtoolbartop.emptyMsg', 'MDC', 'There are no shared communication schedules to display'),
                items: [
                    {
                        text: Uni.I18n.translate('communicationSchedule.add', 'MDC', 'Add shared communication schedule'),
                        privileges: Mdc.privileges.CommunicationSchedule.admin,
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
                itemsPerPageMsg: Uni.I18n.translate('communicationschedule.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Shared communication schedules per page')
            }
        ];

        this.callParent();
    }
});
