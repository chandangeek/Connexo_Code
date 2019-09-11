/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.EditGroupsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.view.creationrules.ExcludeDeviceGroupsGrid'
    ],
    alias: 'widget.issues-creation-rules-edit-groups-form',
    returnLink: null,
    ui: 'large',
    initComponent: function () {
    
        var me = this;

        me.items = [
            {
                xtype: 'isu-device-groups-selection-grid',
                itemId: 'isu-device-groups-selection-grid-id'
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                labelWidth: 0,
                defaultType: 'button',
                items: [
                    {
                        itemId: 'actionOperation',
                        ui: 'action',
                        text: Uni.I18n.translate('general.add', 'ISU', 'Add'),
                        action: 'saveGroupExclusions'
                    },
                    {
                        itemId: 'cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        action: 'cancelGroupExclusions',
                        ui: 'link',
                        href: me.returnLink
                    }
                ]
            }
        ];
        
        me.callParent(arguments);
    }
});