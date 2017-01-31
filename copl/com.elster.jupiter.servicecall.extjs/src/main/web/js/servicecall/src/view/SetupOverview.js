/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.SetupOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalls-setup-overview',
    router: null,
    serviceCallId: null,
    servicecallParam: null,
    store: null,
    activeTab: 0,
    breadcrumbs: null,
    record: null,

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
                    activeTab: me.activeTab,
                    breadcrumbs: me.breadcrumbs,
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'SCS', 'Specifications'),
                            itemId: 'specifications-tab',
                            items: [
                                {
                                    xtype: 'scs-landing-page',
                                    router: me.router,
                                    record: me.record,
                                    title: 'none'
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                            itemId: 'grid-tab',
                            items: [
                                {
                                    xtype: 'service-call-filter',
                                    itemId: 'serviceCallFilter'
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