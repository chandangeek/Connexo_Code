Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.communicationSchedulePreview',
    itemId: 'communicationSchedulePreview',
    requires: [
        'Mdc.model.DeviceType',
        'Mdc.util.ScheduleToStringConverter',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'panel',
            border: false,
            padding: '0 10 0 10',
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('communicationschedule.noCommunicationScheduleSelected', 'MDC', 'No communication schedule selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('communicationschedule.selectCommunicationSchedule', 'MDC', 'Select a communication schedule to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'communicationSchedulePreviewForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>Communication schedule</h4>',
                    itemId: 'communicationSchedulePreviewTitle'
                },
                '->',

                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                    iconCls: 'x-uni-action-iconA',
                    menu: {
                        xtype: 'communication-schedule-action-menu'
                    }
                }
            ],
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
                    },
                    padding: '10 0 0 0',
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
                                    fieldLabel: Uni.I18n.translate('communicationschedule.name', 'MDC', 'Name'),
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
                                    fieldLabel: Uni.I18n.translate('communicationschedule.communicationTasks', 'MDC', 'Communication tasks'),
                                    items: [
                                        {
                                            xtype: 'container',
                                            itemId: 'comTaskPreviewContainer',
                                            layout: {
                                                type: 'vbox'
                                            },
                                            items: [
                                            ]
                                        }
                                    ]
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'schedulingStatus',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.status', 'MDC', 'Status'),
                                    readOnly: true
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
                                    renderer: function(value){
                                        if(value!==null){
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
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

    initComponent: function () {
        this.callParent(arguments);
    }
});


