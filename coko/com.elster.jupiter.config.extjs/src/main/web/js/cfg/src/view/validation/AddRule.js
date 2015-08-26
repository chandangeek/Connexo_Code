Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addRule',
    itemId: 'addRule',
    overflowY: true,

    requires: [
        'Cfg.store.Validators',
        'Cfg.model.Validator',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
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
                    layout: {
                        type: 'vbox'
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
                            fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),
                            itemId: 'addRuleName',
                            required: true,
                            labelAlign: 'right',
                            msgTarget: 'under',
                            maxLength: 80,
                            enforceMaxLength: true,
                            labelWidth: 260,
                            name: 'name',
                            width: 600
                        },
                        {
                            xtype: 'combobox',
                            itemId: 'validatorCombo',
                            msgTarget: 'under',
                            editable: 'false',
                            name: 'implementation',
                            store: 'Validators',
                            valueField: 'implementation',
                            displayField: 'displayName',
                            queryMode: 'local',
                            fieldLabel: Uni.I18n.translate('validation.validator', 'CFG', 'Validator'),
                            required: true,
                            labelAlign: 'right',
                            forceSelection: true,
                            emptyText: Uni.I18n.translate('validation.selectARule', 'CFG', 'Select a rule') + '...',
                            labelWidth: 260,
                            width: 600
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading types'),
                            itemId: 'readingTypesFieldContainer',
                            required: true,
                            msgTarget: 'under',
                            labelWidth: 260,
                            width: 1200,
                            items: [
                                {
                                    xtype: 'panel',
                                    width: 800,
                                    items: [
                                        {
                                            xtype: 'gridpanel',
                                            itemId: 'readingTypesGridPanel',
                                            store: 'ReadingTypesForRule',
                                            hideHeaders: true,
                                            padding: 0,
                                            columns: [
                                                {
                                                    xtype: 'reading-type-column',
                                                    dataIndex: 'readingType',
                                                    flex: 1
                                                },
                                                {
                                                    xtype: 'actioncolumn',
                                                    align: 'right',
                                                    items: [
                                                        {
                                                            iconCls: 'uni-icon-delete',
                                                            handler: function (grid, rowIndex) {
                                                                grid.getStore().removeAt(rowIndex);
                                                            }
                                                        }
                                                    ]
                                                }
                                            ],
                                            height: 220
                                        }
                                    ],
                                    rbar: [
                                        {
                                            xtype: 'container',
                                            items: [
                                                {
                                                    xtype: 'button',
                                                    itemId: 'addReadingTypeButton',
                                                    text: Uni.I18n.translate('validation.addReadingTypes', 'CFG', 'Add reading types'),
                                                    margin: '0 0 0 10'
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'label',
                            cls: 'x-form-invalid-under',
                            itemId: 'readingTypesErrorLabel',
                            margin: '0 0 0 275'
                        },
                        {
                            xtype: 'radiogroup',
                            itemId: 'dataQualityLevel',
                            required: true,
                            labelWidth: 260,
                            width: 600,
                            fieldLabel: Uni.I18n.translate('validation.dataQualityLevel', 'CFG', 'Data quality level'),
                            columns: 1,
                            vertical: true,
                            items: [
                                {
                                    boxLabel: Uni.I18n.translate('validation.dataQualityLevelFail', 'CFG', 'Suspect'),
                                    name: 'action',
                                    inputValue: 'FAIL',
                                    checked:true
                                },
                                {
                                    boxLabel: Uni.I18n.translate('validation.dataQualityLevelWarnOnly', 'CFG', 'Informative'),
                                    name: 'action',
                                    inputValue: 'WARN_ONLY'
                                },
                            ]
                        },
                        {
                            xtype: 'property-form',
                            padding: '5 10 0 10',
                            width: '100%'
                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '20 0 0 0',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth: 260,
                            layout: 'hbox',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'createRuleAction',
                                    itemId: 'createRuleAction'
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                    href: '#/administration/validation',
                                    itemId: 'cancelAddRuleLink',
                                    ui: 'link'
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

