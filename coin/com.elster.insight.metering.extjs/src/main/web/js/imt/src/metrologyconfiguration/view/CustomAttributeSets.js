Ext.define('Imt.metrologyconfiguration.view.CustomAttributeSets', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.custom-attribute-sets',
    itemId: 'custom-attribute-sets',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Uni.view.container.PreviewContainer',
        'Imt.customattributesets.view.Grid',
        'Imt.customattributesets.view.DetailForm',
        'Imt.metrologyconfiguration.store.CustomAttributeSets',
        'Imt.metrologyconfiguration.view.CustomAttributeSetsActions'
    ],
    router: null,

    initComponent: function () {
        var me = this,
            router = me.router,
            casAddRoute = router.getRoute('administration/metrologyconfiguration/view/customAttributeSets/add');

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: router.getRoute().getTitle(),
            items: {
                xtype: 'preview-container',
                grid: {
                    xtype: 'cas-grid',
                    store: 'Imt.metrologyconfiguration.store.CustomAttributeSets',
                    actionColumnConfig: {
                        //privileges: Imt.privileges.MetrologyConfig.admin,
                        xtype: 'uni-actioncolumn',
                        menu: {
                            //privileges: Imt.privileges.MetrologyConfig.admin,
                            xtype: 'custom-attribute-sets-actions'
                        }
                    },
                    dockedConfig: {
                        showTop: true,
                        showBottom: true,
                        showAddBtn: true
                    }
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-comservers',
                    title: Uni.I18n.translate('Imt.metrologyconfiguration.empty.title', 'MDC', 'No custom attribute sets found'),
                    reasons: [
                        Uni.I18n.translate('Imt.metrologyconfiguration.empty.list.item1', 'MDC', 'No custom attribute sets added yet'),
                        Uni.I18n.translate('Imt.metrologyconfiguration.empty.list.item2', 'MDC', 'No custom attribute sets defined yet')
                    ],
                    stepItems: [
                        {
                            text: casAddRoute.getTitle(),
                            itemId: 'add-custom-attribute-set',
                            //privileges: Mdc.privileges.Communication.admin,
                            action: 'addCustomAttributeSet',
                            href: casAddRoute.buildUrl()
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'panel',
                    frame: true,
                    items: {
                        xtype: 'cas-detail-form'
                    },
                    tools: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                            itemId: 'actionButton',
                            iconCls: 'x-uni-action-iconD',
                            menu: {
                                xtype: 'custom-attribute-sets-actions',
                                record: me.record
                            }
                        }
                    ]
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

        me.callParent(arguments);
        me.down('button[action="addAttributeSets"]').on('click', function() {casAddRoute.forward()});
    }
});