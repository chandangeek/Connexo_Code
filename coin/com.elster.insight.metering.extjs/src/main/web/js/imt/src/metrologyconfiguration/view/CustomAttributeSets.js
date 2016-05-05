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
        'Imt.metrologyconfiguration.view.CustomAttributeSetsActions',
        'Uni.util.FormInfoMessage',
        'Uni.util.FormEmptyMessage'
    ],
    router: null,
    metrologyConfiguration: null,

    initComponent: function () {
        var me = this,
            router = me.router,
            casAddRoute = router.getRoute('administration/metrologyconfiguration/view/customAttributeSets/add'),
            isActive = me.metrologyConfiguration.get('active');

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: router.getRoute().getTitle(),
            items: [
                {
                    xtype: 'uni-form-empty-message',
                    text: Uni.I18n.translate('Imt.metrologyconfiguration.error.active', 'IMT', 'You cannot edit custom attribute set because the metrology configuration is active'),
                    hidden: !isActive
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'cas-grid',
                        itemId: 'cas-grid',
                        store: 'Imt.metrologyconfiguration.store.CustomAttributeSets',
                        actionColumnConfig: {
                            header: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                            privileges: Imt.privileges.MetrologyConfig.admin,
                            xtype: 'actioncolumn',
                            align: 'right',
                            width: 80,
                            items: [
                                {
                                    tooltip: Uni.I18n.translate('general.remove', 'IMT', 'Remove'),
                                    iconCls: ' uni-icon-delete',
                                    disabled: isActive,
                                    handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                                        this.fireEvent('deleteCAS', record);
                                    }
                                }
                            ],
                            isDisabled: function() {
                                return isActive;
                            }
                        },
                        dockedConfig: {
                            showTop: true,
                            showBottom: true,
                            showAddBtn: {disabled: isActive}
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'cas-no-items-found-panel',
                        title: Uni.I18n.translate('Imt.metrologyconfiguration.empty.title', 'IMT', 'No custom attribute sets found'),
                        reasons: [
                            Uni.I18n.translate('Imt.metrologyconfiguration.empty.list.item1', 'IMT', 'No custom attribute sets added yet'),
                            Uni.I18n.translate('Imt.metrologyconfiguration.empty.list.item2', 'IMT', 'No custom attribute sets defined yet')
                        ],
                        stepItems: [
                            {
                                text: casAddRoute.getTitle(),
                                itemId: 'add-custom-attribute-set',
                                privileges: Imt.privileges.MetrologyConfig.admin,
                                action: 'addCustomAttributeSet',
                                href: casAddRoute.buildUrl()
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'cas-detail-form',
                        frame: true,
                        tools: [
                            {
                                xtype: 'button',
                                privileges: Imt.privileges.MetrologyConfig.admin,
                                disabled: isActive,
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
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: router,
                        metrologyConfig: me.metrologyConfiguration
                    }
                ]
            }
        ];

        me.callParent(arguments);
        var btn = me.down('button[action="addAttributeSets"]');
        if (btn) {
            btn.on('click', function () {
                casAddRoute.forward()
            });
        }
    }
});