Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedulePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.addSharedCommunicationSchedulePreview',
    itemId: 'addSharedCommunicationSchedulePreview',
    requires: [
        'Mdc.util.ScheduleToStringConverter'
    ],
    hidden: true,
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
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
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
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationSchedule.frequency', 'MDC', 'Frequency'),
                                    renderer: function (value) {
                                        return Mdc.util.ScheduleToStringConverter.convert(value);
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'plannedDate',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationSchedule.plannedDate', 'MDC', 'Planned date'),
                                    renderer: function (value) {
                                        if (value !== null) {
                                            return Uni.DateTime.formatDateTimeLong(value);
                                        } else {
                                            return '';
                                        }
                                    }
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