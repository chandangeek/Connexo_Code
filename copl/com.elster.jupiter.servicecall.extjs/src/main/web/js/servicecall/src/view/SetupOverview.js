Ext.define('Scs.view.SetupOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalls-setup-overview',
    router: null,
    serviceCallId: null,
    store: null,

    requires: [
        'Scs.view.ServiceCallPreviewContainer',
        'Scs.view.ServiceCallFilter',
        'Scs.view.Landing',
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
            items: [
                {
                    xtype: 'tabpanel',
                    ui: 'large',
                    itemId: 'service-call-overview-tab',
                    activeTab: 1,
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'SCS', 'Specifications'),
                            itemId: 'specifications-tab',
                            items: [
                                {
                                    xtype: 'scs-landing-page',
                                    serviceCallId: me.serviceCallId,
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
                /*,
                {
                    xtype: 'tabpanel',
                    ui: 'large',
                    title: Uni.I18n.translate('general.specifications', 'SCS', 'Specifications'),
                    itemId: 'service-call-grid-tab2',
                    items: [
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                            items: []
                        }
                    ]
                }*/
            ]
        };

        me.callParent(arguments);
    }
});