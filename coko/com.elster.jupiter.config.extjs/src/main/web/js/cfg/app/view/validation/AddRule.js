Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addRule',
    itemId: 'addRule',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [

    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Add Rule</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'form',
                    itemId: 'addRuleForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: 500,

                    items: [

                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth:	150,
                            layout: 'hbox',
                            defaults: {
                                //flex: 1,
                                //hideLabel: true
                            },
                            items: [
                                {
                                    text: 'Create',
                                    xtype: 'button',
                                    action: 'addRuleAction',
                                    itemId: 'addRuleAction',
                                    width: 100
                                },
                                {
                                    xtype: 'component',
                                    html: '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation">Cancel</a>',
                                    margin: '0 0 0 20',
                                    width: 100
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

