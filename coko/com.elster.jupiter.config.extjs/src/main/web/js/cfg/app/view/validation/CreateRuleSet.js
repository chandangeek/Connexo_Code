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
                    html: '<h1>Create rule set</h1>',
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
                            validator:function(text){
                                if(Ext.util.Format.trim(text).length==0)
                                    return 'This field is required';
                                else
                                    return true;
                            },
                            required: true,
                            msgTarget: 'under',
                            fieldLabel: 'Name',
                            labelAlign: 'right',
                            labelWidth:	150,
                            maxLength: 80,
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'textarea',
                            name: 'description',
                            fieldLabel: 'Description',
                            labelWidth:	150,
                            maxLength: 256,
                            enforceMaxLength: true
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
                                    padding: '3 0 0 0',
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

