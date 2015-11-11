Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigValRulesSetSetup',
    itemId: 'metrologyConfigValRulesSetSetup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSideMenu',
        'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',
    ],
    router: null,

    content: [
              {
                  xtype: 'panel',
                  ui: 'large',
                  itemId: 'metrologyConfigValRulesSetSetupPanel',
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
                        xtype: 'metrology-config-valrulesset-side-menu',
                        itemId: 'metrology-config-valrulesset-side-menu',
                        router: me.router,
                    }
                ]
            }
        ];
        this.callParent(arguments);
        
        me.down('#metrologyConfigValRulesSetSetupPanel').add(
                {
                    xtype: 'panel',
                    layout: {
                        type: 'hbox'
                    },
                    defaults: {
                        style: {
                            marginRight: '20px',
                            padding: '20px'
                        },
                        flex: 1
                    },
                    items: [
                        {
                            xtype: 'metrologyConfigValRulesSetEdit',
                            ui: 'tile',
                            itemId: 'metrologyConfigValRulesSetEdit',
                            router: me.router,
                            mcid: me.mcid,
                        }
                    ]
                }
            );
        }
    });