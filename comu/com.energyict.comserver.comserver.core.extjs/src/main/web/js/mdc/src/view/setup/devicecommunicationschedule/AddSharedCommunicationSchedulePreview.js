Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedulePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.addSharedCommunicationSchedulePreview',
    itemId: 'addSharedCommunicationSchedulePreview',
    requires: [
        'Mdc.util.ScheduleToStringConverter'
    ],
    hidden: true,
//    header: true,
//    title: ' ',
    items: [
        {
            xtype: 'form',
            border: false,
            itemId: 'addSharedCommunicationSchedulePreviewForm',
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
                                    name: 'temporalExpression',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationSchedule.schedule', 'MDC', 'Schedule'),
                                    renderer: function(value){
                                        return Mdc.util.ScheduleToStringConverter.convert(value);
                                    }

                                },
                                {
                                    xtype: 'fieldcontainer',
                                    name: 'communicationTasks',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationSchedule.communicationTasks', 'MDC', 'Communication tasks'),
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



