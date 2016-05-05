Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSummary',
        'Imt.usagepointmanagement.view.UsagePointMetrologyConfig',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.model.MetrologyConfigOnUsagePoint',
        'Imt.usagepointmanagement.view.SetupActionMenu',
        'Uni.view.widget.WhatsGoingOn'
    ],

    router: null,
    usagePoint: null,
    purposes: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'usage-point-content',
                title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                ui: 'large',
                tools: [
                    {
                        xtype: 'displayfield',
                        itemId: 'usage-point-last-updated-date',
                        value: Uni.I18n.translate('general.lastUpdatedAt', 'IMT', 'Last updated at {0}', [Uni.DateTime.formatDateTimeShort(new Date())], false),
                        height: 27 // just for aligning
                    },
                    {
                        xtype: 'button',
                        itemId: 'usage-point-refresh-data',
                        text: Uni.I18n.translate('general.refresh', 'IMT', 'Refresh'),
                        iconCls: 'icon-spinner12',
                        iconAlign: 'left',
                        margin: '0 16 0 10',
                        handler: function () {
                            me.router.getRoute().forward();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'usage-point-setup-actions-btn',
                        iconCls: 'x-uni-action-iconD',
                        text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                        menu: {
                            xtype: 'usage-point-setup-action-menu',
                            itemId: 'usage-point-setup-action-menu-id',
                            router: me.router
                        }
                    }
                ],
                layout: {
                    type: 'hbox',
                    align: 'stretchmax'
                },
                items: [
                    {
                        xtype: 'container',
                        flex: 2,
                        items: [
                            //{
                            //    itemId: 'usage-point-going-on',
                            //    title: 'lllll',//Uni.I18n.translate('general.whatsGoingOn', 'IMT', "What's going on"),
                            //    ui: 'tile2',
                            //    flex: 1,
                            //    minHeight: 185 // todo: remove after implementation of content for this panel
                            //},
                            {
                                xtype: 'whatsgoingon',
                                mrId: me.usagePoint.get('mRID'),
                                type: 'usagepoint',
                                router: me.router,
                                autoBuild: true,
                                style: 'margin-bottom: 20px'
                            },
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretchmax'
                                },
                                defaults: {
                                    ui: 'tile2',
                                    flex: 1
                                },
                                items: [
                                    {
                                        xtype: 'usage-point-metrology-config',
                                        itemId: 'usage-point-metrology-config',
                                        title: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                                        router: me.router
                                    },
                                    {
                                        itemId: 'usage-point-validation-configuration',
                                        title: Uni.I18n.translate('general.validationConfiguration', 'IMT', 'Validation configuration')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'usage-point-summary',
                        itemId: 'usage-point-summary',
                        title: Uni.I18n.translate('general.usagePointSummary', 'IMT', 'Usage point summary'),
                        ui: 'tile2',
                        flex: 1,
                        router: me.router
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

        me.loadUsagePoint(me.usagePoint);
    },

    loadUsagePoint: function (usagePoint) {
        var me = this;

        if (usagePoint) {
            me.down('#usage-point-summary').loadRecord(usagePoint);
            me.down('#usage-point-metrology-config').loadRecord(new Imt.usagepointmanagement.model.MetrologyConfigOnUsagePoint(usagePoint.get('metrologyConfiguration')));
            me.usagePoint = usagePoint;
        }
    }
});