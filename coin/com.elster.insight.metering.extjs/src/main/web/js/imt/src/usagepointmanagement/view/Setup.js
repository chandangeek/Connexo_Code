Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSummary',
        'Imt.usagepointmanagement.view.UsagePointMetrologyConfig',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.model.MetrologyConfigOnUsagePoint',
        'Imt.usagepointmanagement.view.SetupActionMenu',
        'Imt.usagepointmanagement.view.widget.DataCompletion',
        'Uni.view.button.MarkedButton',
        'Uni.view.widget.WhatsGoingOn'
    ],

    router: null,
    usagePoint: null,
    purposes: null,
    padding: '0 16 16 0',
    favoriteRecord: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                itemId: 'usage-point-content',
                title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                ui: 'large',
                tools: [
                    {
                        xtype: 'marked-button',
                        itemId: 'usage-point-favorite-flag',
                        record: me.favoriteRecord,
                        markedTooltip: Uni.I18n.translate('usagePoint.flag.tooltip.unflag', 'IMT', 'Click to remove from the list of flagged usage point'),
                        unmarkedTooltip: Uni.I18n.translate('usagePoint.flag.tooltip.flag', 'IMT', 'Click to flag the usage point'),
                        width: 20,
                        height: 20
                    },
                    {
                        xtype: 'tbfill'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'usage-point-last-updated-date',
                        value: Uni.I18n.translate('general.lastUpdatedAt', 'IMT', 'Last updated at {0}', [Uni.DateTime.formatTimeShort(new Date())], false),
                        height: 27 // just for aligning
                    },
                    {
                        xtype: 'button',
                        itemId: 'usage-point-refresh-data',
                        text: Uni.I18n.translate('general.refresh', 'IMT', 'Refresh'),
                        iconCls: 'icon-spinner11',
                        iconAlign: 'left',
                        margin: '0 16 0 10',
                        handler: function () {
                            me.router.getRoute().forward();
                        }
                    },
                    {
                        xtype: 'uni-button-action',
                        itemId: 'usage-point-setup-actions-btn',
                        margin: 0,
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
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            padding: 10,
                            margin: '10 10 10 0'
                        },
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
                                usagePointId: me.usagePoint.get('name'),
                                type: 'usagepoint',
                                router: me.router,
                                autoBuild: true,
                                //style: {
                                //    'margin-bottom': '10px',
                                //    'margin-top': '16px'
                                //}
                            },
                            {
                                xtype: 'usage-point-metrology-config',
                                itemId: 'usage-point-metrology-config',
                                meterActivationsStore: me.meterActivationsStore,
                                usagePoint: me.usagePoint,
                                title: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                                ui: 'tile2',
                                router: me.router
                            }
                        ]
                    },
                    {
                        xtype: 'usage-point-summary',
                        itemId: 'usage-point-summary',
                        title: Uni.I18n.translate('general.usagePointSummary', 'IMT', 'Usage point summary'),
                        ui: 'tile2',
                        flex: 1,
                        margin: '10 0 10 10',
                        router: me.router
                    }
                ]
            }
        ];

        if (me.purposes.length) {
            me.content.push(
                {
                    xtype: 'data-completion-widget',
                    itemId: 'data-completion-widget',
                    title: Uni.I18n.translate('general.dataCompletion', 'IMT', 'Data completion'),
                    ui: 'tile2',
                    flex: 1,
                    margin : '10 0 10 0',
                    router: me.router,
                    usagePoint: me.usagePoint,
                    purposes: me.purposes
                }
            );
        }

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
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