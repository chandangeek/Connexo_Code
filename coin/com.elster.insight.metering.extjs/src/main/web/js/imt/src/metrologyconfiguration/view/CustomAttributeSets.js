Ext.define('Imt.metrologyconfiguration.view.CustomAttributeSets', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.custom-attribute-sets',
    itemId: 'custom-attribute-sets',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Uni.view.container.PreviewContainer',
        'Imt.customattributesets.view.Grid',
        'Imt.customattributesets.view.DetailForm',
        'Imt.metrologyconfiguration.store.CustomAttributeSets'
    ],
    router: null,

    initComponent: function () {
        var me = this,
            router = me.router;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: router.getRoute().getTitle(),
            items: {
                xtype: 'preview-container',
                grid: {
                    xtype: 'cas-grid',
                    store: 'Imt.metrologyconfiguration.store.CustomAttributeSets'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-comservers',
                    title: Uni.I18n.translate('comserver.empty.title', 'MDC', 'No communication servers found'),
                    reasons: [
                        Uni.I18n.translate('comserver.empty.list.item1', 'MDC', 'No communication servers created yet')
                    ]
                    //stepItems: [
                    //    {
                    //        text: Uni.I18n.translate('comServer.addOnline', 'MDC', 'Add online communication server'),
                    //        itemId: 'btn-no-items-add-online-communication-server',
                    //        //privileges: Mdc.privileges.Communication.admin,
                    //        action: 'addOnlineComServer',
                    //        href: '#/administration/comservers/add/online'
                    //    }
                    //]
                },
                previewComponent: {
                    xtype: 'cas-detail-form'
                }
            }
        };

        //No custom attribute sets found
        //This could be because:
        //    No custom attribute sets added yet
        //No custom attribute sets defined yet

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: router
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});