Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    itemId: 'usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.AssociatedDevices',
        'Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.usagepointmanagement.view.landingpageattributes.UsagePointMainAttributesPanel',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],
    parent: null,
    router: null,
    minWidth: 1600,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'usagePointSetupPanel',
            layout: {
                type: 'fit',
                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
        panel.title = me.router.getRoute().getTitle();
        panel.tools = [
            {
                xtype: 'toolbar',
                margin: '0 20 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'usage-point-setup-actions-btn',
                        //iconCls: 'x-uni-action-iconD',
                        style: {
                            'background-color': '#71adc7'
                        },
                        text: Uni.I18n.translate('usagepoint.general.setup.actions', 'IMT', 'Actions'),
                        menu: {
                            xtype: 'usage-point-setup-action-menu',
                            itemId: 'usage-point-setup-action-menu-id',
                            router: me.router
                        }

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
                        router: me.router,
                        mRID: me.mRID
                    }
                ]
            }
        ];
        this.callParent(arguments);

        me.down('#usagePointSetupPanel').add(

            {
                xtype: 'container',
                padding: 5,
                layout: {
                    type: 'hbox',
                },
                defaults: {
                    flex: 1
                },
                items: [
                    {
                        style: {
                            marginRight: '20px',
                            padding: '20px'
                        },
                        ui: 'tile',
                        parent: me.parent,
                        title: Uni.I18n.translate('usagepoint.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                        xtype: 'associated-metrology-configuration',
                        router: me.router,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            flex: 1
                        }
                    },
                    {
                        xtype: 'panel',
                        itemId: 'usage-point-attributes-panel',
                        ui: 'tile',
                        style: {
                            marginRight: '20px',
                            padding: '20px'
                        },
                        items: [
                            {
                                xtype: 'panel',
                                itemId: 'usage-point-main-attributes-panel',
                                router: me.router
                            },
                            {
                                xtype: 'custom-attribute-sets-placeholder-form',
                                inline: true,
                                parent: me.parent,
                                itemId: 'custom-attribute-sets-placeholder-form-id',
                                actionMenuXtype: 'usage-point-setup-action-menu',
                                attributeSetType: 'up',
                                router: me.router
                            }
                        ]
                    }
                ]
            }
        );
    }
});