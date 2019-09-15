/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
Ext.define('Uni.property.view.property.enddevicegroups.AddEndDeviceGroupsView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.uni-add-device-groups-view',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
		'Uni.property.view.property.enddevicegroups.AddEndDeviceGroupsGrid'
    ],
    content: [
        {
            ui: 'large',
            itemId: 'uni-add-device-groups-panel',
            title: Uni.I18n.translate('deviceGroupExclusions.excludeEndDeviceGroups', 'UNI', 'Exclude end device groups'),
            items: [
                {
					xtype: 'uni-device-groups-selection-grid',
					itemId: 'uni-device-groups-selection-grid'
				},
				{
					xtype: 'fieldcontainer',
					ui: 'actions',
					labelWidth: 0,
					defaultType: 'button',
					items: [
						{
							itemId: 'saveOperation',
							ui: 'action',
							text: Uni.I18n.translate('general.add', 'ISU', 'Add'),
							action: 'saveGroupExclusions'
						},
						{
							itemId: 'cancelOperation',
							text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
							action: 'cancelGroupExclusions',
							ui: 'link',
						}
					]
				}
            ]
        }
    ]
});
