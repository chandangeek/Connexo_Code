Ext.define('Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanningGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.DeviceCommunicationPlanningGrid',
    ui: 'large',
    cls: 'no-side-padding',
    title: Uni.I18n.translate('deviceCommunicationPlanning.title', 'MDC', 'Communication planning'),

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action'
    ],

    scheduleStore: undefined,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                itemId: 'mdc-device-communication-planning-grid-msg',
                hidden: true,
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('deviceCommunicationPlanning.noCommunicationTasks', 'MDC', 'This device has no communication tasks.')
                    }
                ]
            },
            {
                xtype: 'grid',
                store: me.scheduleStore,
                columns: [
                    {
                        header: Uni.I18n.translate('deviceCommunicationSchedules.communicationTask', 'MDC', 'Commmunication task'),
                        flex: 2,
                        dataIndex: 'comTask',
                        renderer: function (value) {
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }
                            return Ext.String.htmlEncode(value.name);
                        }
                    },
                    {
                        header: Uni.I18n.translate('general.schedule', 'MDC', 'Schedule'),
                        flex: 2,
                        dataIndex: 'schedule',
                        renderer: function (value, meta, record) {
                            if (Ext.isEmpty(value) || record.get('type')==='ONREQUEST') {
                                return '-';
                            }
                            return Mdc.util.ScheduleToStringConverter.convert(value, true);
                        }
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationPlanning.sharedCommunicationSchedule', 'MDC', 'Shared commmunication schedule'),
                        dataIndex: 'name',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationPlanning.plannedDate', 'MDC', 'Planned date'),
                        dataIndex: 'plannedDate',
                        flex: 1,
                        renderer: function (value, meta, record) {
                            if (Ext.isEmpty(value)) {
                                return '-';
                            }

                            var plannedDate = moment(value).valueOf(),
                                nextCommunication = Ext.isEmpty(record.get('nextCommunication')) ? undefined : moment(record.get('nextCommunication')).valueOf();
                            if (plannedDate !== nextCommunication) {
                                var toolTip = !record.get('active')
                                    ? Uni.I18n.translate('deviceCommunicationPlanning.tooltip.inactiveCommunicationTask',
                                        'MDC', 'This communication task is inactive on this device.')
                                    : ( Uni.I18n.translate('deviceCommunicationPlanning.tooltip.nextCommunication.line1',
                                        'MDC', 'Connection method has the strategy \'Minimise connections\'.')
                                        + '&lt;br/&gt;'
                                        + Uni.I18n.translate('deviceCommunicationPlanning.tooltip.nextCommunication.line2',
                                          'MDC', 'The next communication is on {0}.', Uni.DateTime.formatDateTimeShort(nextCommunication) )
                                      );

                                return '<span style="display:inline-block; float:left; margin-right:7px;" >' + Uni.DateTime.formatDateTimeShort(new Date(value)) + '</span>' +
                                    '<span class="icon-warning" style="display:inline-block; color:#EB5642; font-size:16px;" data-qtip="' + toolTip + '"></span>';
                            }
                            return Uni.DateTime.formatDateTimeShort(new Date(value));
                        }
                    },
                    {
                        xtype: 'uni-actioncolumn',
                        menu: {
                            plain: true,
                            border: false,
                            shadow: false,
                            items: [
                                //{
                                //    text: Uni.I18n.translate('deviceCommunicationSchedules.addFrequency', 'MDC', 'Add frequency'),
                                //    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                //    itemId: 'addCommunicationSchedule',
                                //    action: 'addCommunicationSchedule'
                                //
                                //},
                                //{
                                //    text: Uni.I18n.translate('deviceCommunicationSchedules.run', 'MDC', 'Run'),
                                //    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                //    itemId: 'runCommunicationSchedule',
                                //    action: 'runCommunicationSchedule'
                                //
                                //}
                            ]
                        }
                    }
                ],

                dockedItems: [
                    {
                        xtype: 'pagingtoolbartop',
                        noBottomPaging: true,
                        exportButton: false,
                        store: me.scheduleStore,
                        dock: 'top',
                        displayMsg: Uni.I18n.translate('deviceCommunicationPlanning.displayMsg', 'MDC', '{0} - {1} of {2} communication tasks'),
                        displayMoreMsg: '',
                        emptyMsg: '',
                        items: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('deviceCommunicationPlanning.removeSchedule', 'MDC', 'Remove shared communication schedule'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                action: 'addSharedCommunicationSchedule',
                                itemId: 'mdc-device-communication-planning-removeSharedCommunicationScheduleButton'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('deviceCommunicationPlanning.addSchedule', 'MDC', 'Add shared communication schedule'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                action: 'addSharedCommunicationSchedule',
                                itemId: 'mdc-device-communication-planning-addSharedCommunicationScheduleButton'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }

});
