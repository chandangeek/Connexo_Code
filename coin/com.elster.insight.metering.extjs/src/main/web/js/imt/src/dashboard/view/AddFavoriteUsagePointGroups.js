Ext.define('Imt.dashboard.view.AddFavoriteUsagePointGroups', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-favorite-usage-point-groups',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.dashboard.view.FavoriteUsagePointGroups',
        'Imt.privileges.UsagePointGroup'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('selectFavoriteUsagePointGroups.title', 'IMT', 'Select favorite usage point groups'),
                itemId: 'select-usage-point-groups',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            itemId: 'usage-point-groups-grid',
                            xtype: 'favorite-usage-point-groups'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-no-usage-point-group',
                            title: Uni.I18n.translate('selectFavoriteUsagePointGroups.empty.title', 'IMT', 'No usage point group found'),
                            reasons: [
                                Uni.I18n.translate('selectFavoriteUsagePointGroups.empty.list.item0', 'IMT', 'No usage point groups have been defined.')
                            ],
                            stepItems: [
                                {
                                    itemId: 'btn-add-usage-point-group',
                                    text: Uni.I18n.translate('selectFavoriteUsagePointGroups.addUsagePointGroup', 'IMT', 'Add usage point group'),
                                    privileges: Imt.privileges.UsagePointGroup.administrate,
                                    href: '#/usagepoints/usagepointgroups/add'
                                }
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});