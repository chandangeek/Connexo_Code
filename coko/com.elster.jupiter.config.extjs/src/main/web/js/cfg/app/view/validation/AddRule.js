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
                    html: '<h1>' + Uni.I18n.translate('validation.addRule', 'CFG', 'Add rule') + '</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'form',
                            itemId: 'addRuleForm',
                            padding: '10 10 0 10',
                            width: 700,
                            layout: {
                                type: 'vbox'//,
                                //align: 'stretch'
                            },
                            items:[
                                {
                                    xtype: 'textfield',
                                    fieldLabel:  Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                                    labelAlign: 'right',
                                    validator:function(text){
                                        if(Ext.util.Format.trim(text).length==0)
                                            return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
                                        else
                                            return true;
                                    },
                                    required: true,
                                    msgTarget: 'under',
                                    maxLength: 80,
                                    enforceMaxLength: true,
                                    labelWidth:	250,
                                    name: 'name',
                                    width: 600
                                },
                                {
                                    xtype: 'combobox',
                                    itemId: 'validatorCombo',
                                    validator:function(text){
                                        if(Ext.util.Format.trim(text).length==0)
                                            return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
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
                                    fieldLabel: Uni.I18n.translate('validation.rule', 'CFG', 'Rule'),
                                    labelAlign: 'right',
                                    forceSelection: false,
                                    emptyText: Uni.I18n.translate('validation.selectARule', 'CFG', 'Select a rule') + '...',
                                    labelWidth:	250,
                                    width: 600
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
                                            xtype: 'container',
                                            //itemId: 'readingValuesTextFieldsContainer',
                                            layout: {
                                                type: 'hbox'//,
                                                //align: 'stretch'
                                            },
                                            items: [
                                                {
                                                    xtype: 'textfield',
                                                    fieldLabel: Uni.I18n.translate('validation.readingValues', 'CFG', 'Reading value(s)'),
                                                    labelAlign: 'right',
                                                    itemId: 'readingTypeTextField1',
                                                    vtype: 'readingtype',
                                                    validator:function(text){
                                                        if(Ext.util.Format.trim(text).length==0)
                                                            return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
                                                        else
                                                            return true;
                                                    },
                                                    required: true,
                                                    msgTarget: 'under',
                                                    labelWidth:	250,
                                                    maxLength: 80,
                                                    enforceMaxLength: true ,
                                                    width: 600,
                                                    margin:'0 0 5 0'
                                                }
                                            ]
                                        }

                                    ]

                                },
                                {
                                    xtype: 'fieldcontainer',
                                    margin:'5 0 0 0',
                                    fieldLabel: '&nbsp',
                                    labelAlign: 'right',
                                    labelWidth:	250,
                                    layout: 'hbox',
                                    items: [
                                        {
                                            text: '+ ' + Uni.I18n.translate('validation.addAnother', 'CFG', 'Add another'),
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
                                            text: Uni.I18n.translate('general.create', 'CFG', 'Create'),
                                            xtype: 'button',
                                            action: 'createRuleAction',
                                            itemId: 'createRuleAction',
                                            width: 100
                                        },
                                        {
                                            xtype: 'component',
                                            html: '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation">' + Uni.I18n.translate('general.cancel', 'CFG', 'Cancel') + '</a>',
                                            itemId: 'cancelAddRuleLink',
                                            padding: '3 0 0 0',
                                            margin: '0 0 0 20',
                                            width: 100
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }],

    initComponent: function () {
        this.callParent(arguments);
    }
});

