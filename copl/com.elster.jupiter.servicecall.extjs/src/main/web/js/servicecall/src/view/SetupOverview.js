Ext.define('Scs.view.SetupOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalls-setup-overview',
    router: null,
    serviceCallId: null,
    servicecallParam: null,
    store: null,
    tab: null,
    breadcrumbs: null,

    requires: [
        'Scs.view.ServiceCallPreviewContainer',
        'Scs.view.ServiceCallFilter',
        'Scs.view.Landing'
    ],

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: me.serviceCallId,
            items: [
                {
                    xtype: 'tabpanel',
                    ui: 'large',
                    itemId: 'service-call-overview-tab',
                    activeTab: me.tab === 'grid' ? 1 : 0,
                    breadcrumbs: me.breadcrumbs,
                    servicecallParam: me.servicecallParam,
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'SCS', 'Specifications'),
                            itemId: 'specifications-tab',
                            items: [
                                {
                                    xtype: 'scs-landing-page',
                                    router: me.router,
                                    title: 'none'
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                            itemId: 'grid-tab',
                            items: [
                                {
                                    xtype: 'service-call-filter'
                                },
                                {
                                    xtype: 'service-call-preview-container',
                                    router: me.router,
                                    store: me.store
                                }

                            ]
                        }

                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});