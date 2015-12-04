Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    itemId: 'usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.AssociatedDevices',
        'Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.view.UsagePointAttributesFormMain',
    ],
    router: null,
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
                xtype: 'panel',
                layout: {
                    type: 'hbox'
                },
                defaults: {
                    style: {
//                        marginRight: '20px',
//                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                    	  xtype: 'panel',
                          layout: {
                              type: 'vbox'
                          },
                          defaults: {
                              style: {
//                                  marginRight: '20px',
//                                  padding: '20px'
                              },
                              flex: 1
                          },
                          items: [
                               {
                            	   xtype: 'associated-devices',
                            	   router: me.router,
                            	   width: 400,
                               },
                               {
                            	   xtype: 'associated-metrology-configuration',
                            	   router: me.router,
                            	   width: 400,
                               },
                          ]
                    },
//                   {
//                        xtype: 'panel',
//                        title: Uni.I18n.translate('usagepoint.attributes', 'IMT', 'Usage Point Attributes'),
//                        ui: 'tile',
//                        itemId: 'usage-point-attributes-panel',
//                        router: me.router
//                    }
                    
                    {
                    	  xtype: 'panel',
                          layout: {
                              type: 'vbox'
                          },
                          defaults: {
                              flex: 1
                          },
                          items: [
                               {
                            	   xtype: 'button',
                            	   text: Uni.I18n.translate('usagepoint.general.edit', 'IMT', 'Edit usage point'),
                            	   href: me.router.getRoute('usagepoints/edit').buildUrl({mcid: me.mcid}),
                            	   router: me.router,
                            	   margin: '0 0 0 220',
                            	   aligh: 'right',
                               },
                               {
                            	   xtype: 'panel',
                            	   title: Uni.I18n.translate('usagepoint.attributes', 'IMT', 'Usage Point Attributes'),
                            	   ui: 'tile',
                            	   itemId: 'usage-point-attributes-panel',
                            	   router: me.router
                               },
                          ]
                    	
                    }
                ]
            }
        );
    }
});