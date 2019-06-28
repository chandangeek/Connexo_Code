/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.ExcludeDeviceGroupsWindow', {
    extend: 'Ext.window.Window',
    requires: [
        'Isu.view.creationrules.ExcludeDeviceGroupsGrid'
    ],
    alias: 'widget.issues-creation-rules-exclude-device-groups-window',
    modal: true,
    layout: 'fit',
    title: Uni.I18n.translate('administration.issueCreationRules.selectExcludedGroups', 'ISU', 'Select device groups to exclude'),
    itemId: 'issues-creation-rules-exclude-device-groups-window',
    initComponent: function () {
    
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'exclude-device-groups-form',
            padding: 0,
            defaults: {
                width: 700,
                height: 450
            },
            items: [
                {
                    xtype: 'isu-device-groups-selection-grid',
                    itemId: 'isu-device-groups-selection-grid-id'
                }
            ]
        };

        me.buttons = [
            {
                text: Uni.I18n.translate('general.save', 'ISU', 'Save'),
                ui: 'action',
                action: 'saveGroupExclusions'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                ui: 'action',
                action: 'cancelGroupExclusions'
            }
        ];

        me.callParent(arguments);
    }
});