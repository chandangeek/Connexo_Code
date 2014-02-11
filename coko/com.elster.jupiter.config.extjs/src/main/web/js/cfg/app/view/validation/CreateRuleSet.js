Ext.define('Cfg.view.validation.CreateRuleSet', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.createRuleSet',
    itemId: 'createRuleSet',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Uni.view.breadcrumb.Trail'
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
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'component',
                    html: '<h1>Create new rule set</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'form',
                    itemId: 'newRuleSetForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: 500,

                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            allowBlank: false,  // requires a non-empty value
                            blankText: 'This is a required field',
                            msgTarget: 'under',
                            fieldLabel: 'Name *',
                            labelAlign: 'right',
                            labelWidth:	150
                        },
                        {
                            xtype: 'textarea',
                            name: 'description',
                            fieldLabel: 'Description:',
                            labelWidth:	150
                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '20 0 0 0',
                            fieldLabel: '&nbsp',
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
                                    action: 'createNewRuleSet',
                                    itemId: 'createNewRuleSet',
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

