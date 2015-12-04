Ext.define('Imt.usagepointmanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-management-setup',
    itemId: 'usage-point-management-setup',
    requires: [
        'Imt.usagepointmanagement.view.AssociatedDevices',
        'Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.view.UsagePointAttributesFormMain',
        'Imt.usagepointmanagement.view.UsagePointActionMenu',
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
            },
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
        panel.title = me.router.getRoute().getTitle();
        panel.tools = [
           {
               xtype: 'toolbar',
               margin: '0 100 0 0',
               items: [
                       {
                           xtype: 'button',
                           itemId: 'edit-attribute-btn',
                           style: {
                               'background-color': '#71adc7'
                           },
                           privileges: Imt.privileges.UsagePoint.admin,
                           text: Uni.I18n.translate('usagepoint.general.edit.attributes', 'IMT', 'Edit attributes'),
                           href: me.router.getRoute('usagepoints/edit').buildUrl({mRID: me.mRID})
                       }, 
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
                            	   xtype: 'panel',
                            	   title: Uni.I18n.translate('usagepoint.attributes', 'IMT', 'Usage Point Attributes'),
                            	   ui: 'tile',
                            	   itemId: 'usage-point-attributes-panel',
                            	   router: me.router,
                            	   width: 400,
                            	   align: 'right',
                               },
                          ]
                    	
                    }
                ]
            }
        );
    }
});