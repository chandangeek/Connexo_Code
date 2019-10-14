/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
Ext.define('Uni.property.view.property.comtasks.AddComTasksView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.uni-add-comtasks-view',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
		'Uni.property.view.property.comtasks.AddComTasksGrid'
    ],
    content: [
        {
            ui: 'large',
            itemId: 'uni-add-comtasks-panel',
            title: Uni.I18n.translate('comTaskFiltering.excludeComTasks', 'UNI', 'Exclude communication tasks'),
            items: [
                {
					xtype: 'uni-comtasks-selection-grid',
					itemId: 'uni-comtasks-selection-grid'
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
							action: 'saveComTaskExclusions'
						},
						{
							itemId: 'cancelOperation',
							text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
							action: 'cancelComTaskExclusions',
							ui: 'link',
						}
					]
				}
            ]
        }
    ]
});