/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationschedule.SharedCommunicationSchedulePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    hidden: true,
    alias: 'widget.sharedCommunicationSchedulePreview',
    itemId: 'sharedCommunicationSchedulePreview',
    requires: [
        'Mdc.util.ScheduleToStringConverter'
    ],
    title: '',
    items: [
        {
            xtype: 'form',
            itemId: 'sharedCommunicationSchedulePreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    labelWidth: 250
                },
                {
                    xtype: 'fieldcontainer',
                    name: 'communicationTasks',
                    fieldLabel: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                    labelWidth: 250,
                    items: [
                        {
                            xtype: 'container',
                            itemId: 'comTaskPreviewContainer',
                            items: []
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