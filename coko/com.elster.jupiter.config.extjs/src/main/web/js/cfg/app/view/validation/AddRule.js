Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addRule',
    itemId: 'addRule',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.store.Validators',
        'Cfg.model.Validator',
        'Uni.view.breadcrumb.Trail'
    ],


    readingTypeIndex : 1,


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
                    html: '<h1>Add rule</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'container',
                    cls: 'content-container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'form',
                            itemId: 'addRuleForm',
                            padding: '10 10 0 10',
                            width: 700,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            items:[
                                {
                                    xtype: 'textfield',
                                    fieldLabel: 'Name',
                                    labelAlign: 'right',
                                    validator:function(text){
                                        if(Ext.util.Format.trim(text).length==0)
                                            return 'This field is required';
                                        else
                                            return true;
                                    },
                                    required: true,
                                    msgTarget: 'under',
                                    maxLength: 80,
                                    enforceMaxLength: true,
                                    labelWidth:	250,
                                    name: 'name'
                                },
                                {
                                    xtype: 'combobox',
                                    itemId: 'validatorCombo',
                                    validator:function(text){
                                        if(Ext.util.Format.trim(text).length==0)
                                            return 'This field is required';
                                        else
                                            return true;
                                    },
                                    required: true,
                                    msgTarget: 'under',
                                    editable: 'false',
                                    name: 'implementation',
                                    store: Ext.create('Cfg.store.Validators'),
                                    valueField: 'implementation',
                                    displayField: 'displayName',
                                    queryMode: 'local',
                                    fieldLabel: 'Rule',
                                    labelAlign: 'right',
                                    forceSelection: false,
                                    emptyText: 'Select a rule...',
                                    labelWidth:	250
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'readingValuesTextFieldsContainer',
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },

                                    items: [
                                        {
                                            xtype: 'textfield',
                                            fieldLabel: 'Reading value(s)',
                                            labelAlign: 'right',
                                            itemId: 'readingTypeTextField1',
                                            validator:function(text){
                                                if(Ext.util.Format.trim(text).length==0)
                                                    return 'This field is required';
                                                else
                                                    return true;
                                            },
                                            required: true,
                                            msgTarget: 'under',
                                            labelWidth:	250,
                                            maxLength: 80,
                                            enforceMaxLength: true
                                        }
                                    ]
                                },
                                {
                                    xtype: 'fieldcontainer',
                                    fieldLabel: '&nbsp',
                                    labelAlign: 'right',
                                    labelWidth:	250,
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
                                    xtype: 'container',
                                    itemId: 'propertiesContainer',
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    margin: '20 0 0 0',

                                    items: []
                                },

                                {
                                    xtype: 'fieldcontainer',
                                    margin: '20 0 0 0',
                                    fieldLabel: '&nbsp',
                                    labelAlign: 'right',
                                    labelWidth:	250,
                                    layout: 'hbox',
                                    items: [
                                        {
                                            text: 'Create',
                                            xtype: 'button',
                                            action: 'createRuleAction',
                                            itemId: 'createRuleAction',
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
                        }, {
                            xtype: 'container',
                            itemId: 'removeReadingTypesButtonsContainer',
                            layout: {
                                type: 'vbox',
                                pack: 'center'
                            },
                            padding: '16 0 10 0',
                            items: [
                                {
                                    xtype: 'component',
                                    margin:'0 0 10 0',
                                    html: '&nbsp'
                                },
                                {
                                    xtype: 'component',
                                    margin:'0 0 10 0',
                                    html: '&nbsp'
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

