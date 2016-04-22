Ext.define('Imt.purpose.view.Outputs', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.purpose-outputs',
    itemId: 'purpose-outputs',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.purpose.view.OutputsList',
        'Imt.purpose.view.PurposeDetailsForm'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'purpose-outputs',
                title: me.router.getRoute().getTitle(),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'purpose-details-form',
                        record: me.purpose
                    },
                    {
                        xtype: 'emptygridcontainer',
                        grid: {
                            xtype: 'outputs-list',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-metrology-configurations',
                            title: Uni.I18n.translate('outputs.list.empty', 'IMT', 'No outputs is configured for selected purpose')
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                style: {
                    paddingRight: 0
                },
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});