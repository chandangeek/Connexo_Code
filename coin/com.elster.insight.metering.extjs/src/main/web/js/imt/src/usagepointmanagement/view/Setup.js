Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSummary',
        'Imt.usagepointmanagement.view.UsagePointMetrologyConfig',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.model.MetrologyConfigOnUsagePoint'
    ],

    router: null,
    usagePoint: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'usage-point-content',
                title: me.usagePoint ? me.usagePoint.get('mRID') :' ',
                ui: 'large',
                tools: [
                    {
                        xtype: 'displayfield',
                        itemId: 'usage-point-last-updated-date',
                        value: Uni.I18n.translate('general.lastUpdatedAt', 'IMT', 'Last updated at {0}', [Uni.DateTime.formatDateTimeShort(new Date())], false),
                        margin: '0 10 0 0',
                        height: 27 // just for aligning
                    },
                    {
                        xtype: 'button',
                        itemId: 'usage-point-refresh-data',
                        text: Uni.I18n.translate('general.refresh', 'IMT', 'Refresh'),
                        iconCls: 'icon-spinner12',
                        iconAlign: 'left',
                        margin: 0,
                        handler: function () {
                            me.router.getRoute().forward();
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'stretchmax'
                        },
                        items: [
                            {
                                xtype: 'container',
                                flex: 2,
                                items: [
                                    {
                                        itemId: 'usage-point-going-on',
                                        title: Uni.I18n.translate('general.whatsGoingOn', 'IMT', "What's going on"),
                                        ui: 'tile',
                                        margin: '0 16 0 0',
                                        flex: 1,
                                        minHeight: 150 // todo: remove after implementation of content for this panel
                                    },
                                    {
                                        xtype: 'container',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretchmax'
                                        },
                                        defaults: {
                                            ui: 'tile',
                                            flex: 1,
                                            margin: '16 16 0 0'
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
                                title: Uni.I18n.translate('general.summary', 'IMT', 'Summary'),
                                ui: 'tile',
                                margin: 0,
                                flex: 1,
                                router: me.router
                            }
                        ]
                    },
                    {
                        itemId: 'usage-point-activity-line',
                        title: Uni.I18n.translate('general.activityLine', 'IMT', 'Activity line'),
                        ui: 'tile',
                        margin: '16 0 0 0',
                        minHeight: 150 // todo: remove after implementation of content for this panel
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router
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