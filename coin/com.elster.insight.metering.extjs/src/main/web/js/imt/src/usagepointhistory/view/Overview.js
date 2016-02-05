Ext.define('Imt.usagepointhistory.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-history',
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSideMenu'
    ],
    store: 'Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            Ext.apply({
                xtype: 'tabpanel',
                itemId: 'usage-point-history-tab-panel',
                title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                ui: 'large'
            }, me.getTabPanelCfg())

        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        mRID: me.router.arguments.mRID
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    getTabPanelCfg: function () {
        var me = this,
            cfg = {
                activeTab: 0,
                items: []
            };

        Ext.getStore(me.store).each(function (cas) {
            var id;

            if (cas.get('timesliced')) {
                id = cas.get('id');
                cfg.items.push(
                    {
                        title: cas.get('name'),
                        itemId: 'custom-attribute-set-' +  id,
                        customAttributeSetId: id
                    }
                );
                if (id == me.router.queryParams.customAttributeSetId) {
                    cfg.activeTab = cfg.items.length - 1;
                }
            }
        });

        return cfg;
    }
});