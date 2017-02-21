/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.alarm-creation-rules-overview',
    itemId: 'creation-rules-overview',

    requires: [
        'Dal.view.creationrules.List',
        'Dal.view.creationrules.Item',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.alarmCreationRules', 'DAL', 'Alarm creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'alarms-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.alarmCreationRules.empty.title', 'DAL', 'No alarm creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.alarmCreationRules.empty.list.item1', 'DAL', 'No alarm creation rules have been defined yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.alarmCreationRules.add', 'DAL', 'Add rule'),
                                privileges:Dal.privileges.Alarm.createAlarmRule,
                                href: '#/administration/alarmcreationrules/add',
                                action: 'create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'alarm-creation-rules-item'
                    }
                }
            ]
        }
    ]
});