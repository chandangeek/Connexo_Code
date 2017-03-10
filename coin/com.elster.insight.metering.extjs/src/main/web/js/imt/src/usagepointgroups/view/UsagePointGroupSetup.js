/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.UsagePointGroupSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepointgroup-setup',
    xtype: 'usagepointgroup-setup',
    router: null,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                ui: 'large',
                itemId: 'usagepointgroup-setup-panel',
                title: Uni.I18n.translate('general.usagePointGroups', 'IMT', 'Usage point groups'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'usagepointgroup-setup-preview-container',
                        grid: {
                            xtype: 'usagepointgroups-grid',
                            itemId: 'usagepointgroups-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-usagepointgroups-panel',
                            title: Uni.I18n.translate('usagepointgroup.empty.title', 'IMT', 'No usage point groups found'),
                            reasons: [
                                Uni.I18n.translate('usagepointgroup.empty.list.item1', 'IMT', 'No usage point groups have been defined yet.'),
                                Uni.I18n.translate('usagepointgroup.empty.list.item2', 'IMT', 'Usage point groups exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('usagepointgroup.add', 'IMT', 'Add usage point group'),
                                    privileges: Imt.privileges.UsagePointGroup.administrate,
                                    action: 'add-usage-point-group-from-empty-grid',
                                    itemId: 'add-usage-point-group-btn-from-empty-grid'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'usagepointgroup-preview',
                            itemId: 'usagepointgroup-preview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});



