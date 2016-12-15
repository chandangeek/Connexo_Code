Ext.define('Imt.usagepointgroups.view.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepointgroup-details',
    xtype: 'usagepointgroup-details',

    requires: [
        'Yfn.privileges.Yellowfin',
        'Imt.usagepointgroups.view.Menu',
        'Imt.usagepointgroups.view.UsagePointGroupPreview',
        'Imt.usagepointgroups.view.UsagePointGroupActionMenu',
        'Imt.usagepointgroups.view.PreviewForm',        
        'Imt.usagepointgroups.view.UsagePointsOfUsagePointGroupGrid'        
    ],

    router: null,
    usagePointGroup: null,
    favoriteRecord: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usagepointgroups-menu',
                        itemId: 'usagepointgroups-details-menu',
                        router: me.router,
                        usagePointGroup: me.usagePointGroup
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    ui: 'large',
                    header: {
                        title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                        titleIsShrinked: true
                    },
                    flex: 1,
                    items: {
                        xtype: 'usagepointgroup-preview-form',
                        itemId: 'usagepointgroup-details-preview-form',
                        usagePointGroupId: me.usagePointGroup.getId()
                    },
                    tools: [
                        {
                            xtype: 'marked-button',
                            itemId: 'usage-point-group-favorite-flag',
                            record: me.favoriteRecord,
                            markedTooltip: Uni.I18n.translate('usagePointGroup.flag.tooltip.unflag', 'IMT', 'Click to remove from the list of flagged usage point group'),
                            unmarkedTooltip: Uni.I18n.translate('usagePointGroup.flag.tooltip.flag', 'IMT', 'Click to flag the usage point group'),
                            width: 20,
                            height: 20
                        },
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'button',
                            itemId: 'generate-report',
                            privileges: Yfn.privileges.Yellowfin.view,
                            margin: '20 10 0 0',
                            text: Uni.I18n.translate('generatereport.generateReportButton', 'IMT', 'Generate report')
                        },
                        {
                            xtype: 'uni-button-action',
                            itemId: 'usagepointgroup-details-actions-button',
                            margin: '20 0 0 0',
                            privileges: Imt.privileges.UsagePointGroup.administrateAnyOrStaticGroup,
                            menu: {
                                xtype: 'usagepointgroup-action-menu',
                                itemId: 'usagepointgroup-details-action-menu',
                                record: me.usagePointGroup
                            }
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    itemId: 'usagepointgroup-search-preview-container',
                    grid: {
                        xtype: 'usagepoints-of-usagepointgroup-grid',
                        itemId: 'usagepoints-of-usagepointgroup-grid',
                        service: me.service
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'search-preview-container-empty-msg',
                        text: Uni.I18n.translate('usagepointgroup.empty.list.message', 'IMT', 'There are no usagepoints in your group.')
                    }
                }
            ]
        };

        me.callParent(arguments);

        if (me.usagePointGroup) {
            me.down('form').loadRecord(me.usagePointGroup);
        }
    }
});


