Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addRule',
    itemId: 'addRule',
    overflowY: true,
    requires: [
        'Cfg.store.Validators',
        'Cfg.model.Validator'
    ],

    edit: false,

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createRuleAction').setText(Uni.I18n.translate('general.save', 'CFG', 'Save'));
            this.down('#createRuleAction').action = 'editRuleAction';
        } else {
            this.edit = edit;
            this.down('#createRuleAction').setText(Uni.I18n.translate('general.add', 'CFG', 'Add'));
            this.down('#createRuleAction').action = 'createRuleAction';
        }
        this.down('#cancelAddRuleLink').href = returnLink;
    },

    readingTypeIndex: 1,


    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'addRuleTitle',
            items: [
                {
                    xtype: 'form',
                    itemId: 'addRuleForm',
                    padding: '10 10 0 10',
                    width: 700,
                    layout: {
                        type: 'vbox'//,
                        //align: 'stretch'
                    },
                    defaults: {
                        validateOnChange: false,
                        validateOnBlur: false
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            margin: '0 0 10 0',
                            hidden: true
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                            required: true,
                            labelAlign: 'right',
                            msgTarget: 'under',
                            maxLength: 80,
                            enforceMaxLength: true,
                            allowBlank: false,
                            labelWidth: 250,
                            name: 'name',
                            width: 600
                        },
                        {
                            xtype: 'combobox',
                            itemId: 'validatorCombo',
                            msgTarget: 'under',
                            editable: 'false',
                            name: 'implementation',
                            store: Ext.create('Cfg.store.Validators'),
                            valueField: 'implementation',
                            displayField: 'displayName',
                            queryMode: 'local',
                            fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),
                            required: true,
                            allowBlank: false,
                            labelAlign: 'right',
                            forceSelection: false,
                            emptyText: Uni.I18n.translate('validation.selectARule', 'CFG', 'Select a rule') + '...',
                            labelWidth: 250,
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
                                            fieldLabel: Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading type(s)'),
                                            labelAlign: 'right',
                                            itemId: 'readingType1',
                                            name: 'readingType1',
                                            /*vtype: 'readingtype',
                                             validator:function(text){
                                             if(Ext.util.Format.trim(text).length==0)
                                             return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
                                             else
                                             return true;
                                             },
                                             required: true,  */
                                            msgTarget: 'under',
                                            labelWidth: 250,
                                            maskRe: /^($|\S.*$)/,
                                            required: true,
                                            allowBlank: false,
                                            validateOnChange: false,
                                            validateOnBlur: false,
                                            maxLength: 80,
                                            enforceMaxLength: true,
                                            width: 600,
                                            margin: '0 0 5 0'
                                        }
                                    ]
                                }

                            ]

                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '5 0 0 0',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth: 250,
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
                            labelWidth: 250,
                            layout: 'hbox',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'createRuleAction',
                                    itemId: 'createRuleAction',
                                    width: 100
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                    href: '#/administration/validation',
                                    itemId: 'cancelAddRuleLink',
                                    ui: 'link',
                                    padding: '3 0 0 0',
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
        this.setEdit(this.edit, this.returnLink);
    }
});

