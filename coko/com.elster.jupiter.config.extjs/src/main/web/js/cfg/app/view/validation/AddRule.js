Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addRule',
    itemId: 'addRule',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.store.Validators',
        'Cfg.model.Validator'
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
                    //width: 500,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items:[
                        {
                            xtype: 'combobox',
                            name: 'implementation',
                            store: Ext.create('Cfg.store.Validators'),
                            valueField: 'implementation',
                            displayField: 'displayName',
                            queryMode: 'local',
                            fieldLabel: 'Rule:',
                            labelAlign: 'right',
                            forceSelection: false,
                            emptyText: 'Select a rule...',
                            labelWidth:	150,
                            width: 400
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Reading value(s):',
                            labelAlign: 'right',
                            labelWidth:	150,
                            width: 400
                        },

                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth:	150,
                            //width: 430,
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    /*fieldLabel: '&nbsp',
                                    labelAlign: 'right',
                                    labelWidth:	150,   */
                                    width: 400
                                },
                                {
                                    text: '-',
                                    xtype: 'button',
                                    action: 'removeReadingTypeAction',
                                    itemId: 'removeReadingTypeAction',
                                    margin: '0 0 0 10',
                                    width: 30
                                }
                            ]
                        },

                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth:	150,
                            layout: 'hbox',
                            items: [
                                {
                                    text: '+ Add another',
                                    xtype: 'button',
                                    action: 'addReadingTypeAction',
                                    itemId: 'addReadingTypeAction',
                                    width: 100
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '20 0 0 0',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth:	150,
                            layout: 'hbox',
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
                }]
            }
        ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

