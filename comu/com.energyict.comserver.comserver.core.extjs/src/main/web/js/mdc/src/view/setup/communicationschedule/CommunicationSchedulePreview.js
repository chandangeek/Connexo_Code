Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.communicationSchedulePreview',
    itemId: 'communicationSchedulePreview',
    requires: [
        'Mdc.model.DeviceType',
        'Mdc.util.ScheduleToStringConverter',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            privileges: Mdc.privileges.CommunicationSchedule.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'communication-schedule-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            border: false,
            itemId: 'communicationSchedulePreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'mRID',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.MRID', 'MDC', 'MRID')

                                },
                                {
                                    xtype: 'fieldcontainer',
                                    name: 'communicationTasks',
                                    fieldLabel: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                                    items: [
                                        {
                                            xtype: 'container',
                                            itemId: 'comTaskPreviewContainer',
                                            items: [
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'temporalExpression',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                                    renderer: function(value){
                                        return Mdc.util.ScheduleToStringConverter.convert(value);
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'plannedDate',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                }

                            ]
                        }

                    ]
                },
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    border: false,
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'deviceTypeDetailsLink',
                            html: '' // filled in in Controller
                        }

                    ]
                }
            ]
        }
    ],

    emptyText: '<h3>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No shared communication schedule selected') + '</h3><p>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a shared communication schedule to see its details') + '</p>',

    initComponent: function () {
        this.callParent(arguments);
    }
});


