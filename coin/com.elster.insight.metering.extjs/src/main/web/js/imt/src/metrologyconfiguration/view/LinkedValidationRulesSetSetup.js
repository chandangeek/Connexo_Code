Ext.define('Imt.metrologyconfiguration.view.LinkedValidationRulesSetSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.linkedValidationRulesSetSetup',
    itemId: 'linkedValidationRulesSetSetup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSideMenu',
        'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',
        'Cfg.view.validation.RuleSetBrowse'
    ],
    router: null,

    content: [
              {
                  xtype: 'panel',
                  ui: 'large',
                  itemId: 'linkedValidationRulesSetSetupPanel',
                  layout: {
                      type: 'fit',
                      align: 'stretch'
                  }
              }
    ],
    
    initComponent: function () {
        var me = this,
        	panel = me.content[0];
 //       panel.title = me.router.getRoute().getTitle();

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'linked-validation-rulesset-side-menu',
                        itemId: 'linked-validation-rulesset-side-menu',
                        router: me.router,
                    }
                ]
            }
        ];
        this.callParent(arguments);
        
        me.down('#linkedValidationRulesSetSetupPanel').add(
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
                            xtype: 'validationrulesetBrowse',
                            ui: 'tile',
                            itemId: 'validationrulesetBrowse',
                            router: me.router,
                            mcid: me.mcid,
                        }
                    ]
                }
            );
        }
    });