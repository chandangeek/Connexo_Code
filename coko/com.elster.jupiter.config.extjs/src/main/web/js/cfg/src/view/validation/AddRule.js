Ext.define('Cfg.view.validation.AddRule', {
    extend: 'Ext.form.Panel',
    alias: 'widget.addRule',
    itemId: 'addRule',

    requires: [
        'Cfg.store.Validators',
        'Cfg.model.Validator',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],
    ui: 'large',
    padding: '10 10 0 10',

    defaults: {
        labelWidth: 260,
        width: 600
    },

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




    initComponent: function () {


        var me = this;

        me.title = me.edit ? '&nbsp;' : Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule');

        me.items = [
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
                            /*{
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading types'),
                                required: true,
                                msgTarget: 'under',
                                layout: 'hbox',
                                width: 1100,
                                items: [
                                            {
                                                xtype: 'component',
                                                html: Uni.I18n.translate('general.noReadingTypesAvailable','CFG','No reading types have been added'),
                                                itemId: 'noReadingTypesForValidationRuleLabel',
                                                //hidden: true,
                                                style: {
                                                    'font': 'italic 13px/17px Lato',
                                                    'color': '#686868',
                                                    'margin-top': '6px',
                                                    'margin-right': '10px'
                                                }
                                            },
                                            {
                                                xtype: 'gridpanel',
                                                itemId: 'readingTypesForValidationRuleGridPanel',
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
                                                                    if (grid.getStore().count() === 0) {
                                                                        me.updateGrid();
                                                                    }
                                                                }
                                                            }
                                                        ]
                                                    }
                                                ],
                                                height: 220
                                            },
                                            {
                                                        xtype: 'button',
                                                        itemId: 'addReadingTypeButton',
                                                        text: Uni.I18n.translate('validation.addReadingTypes', 'CFG', 'Add reading types'),
                                                        margin: '0 0 0 10'
                                            }
                                ]
                            },*/


                            {
                                xtype: 'fieldcontainer',
                                itemId: 'reading-types-field-container',
                                fieldLabel: Uni.I18n.translate('general.readingTypes', 'EST', 'Reading types'),
                                required: true,
                                layout: 'hbox',
                                width: 1100,
                                items: [
                                    {
                                        xtype: 'component',
                                        html: Uni.I18n.translate('general.noReadingTypesAvailable','CFG','No reading types have been added'),
                                        itemId: 'noReadingTypesForValidationRuleLabel',
                                        //hidden: true,
                                        style: {
                                            'font': 'italic 13px/17px Lato',
                                            'color': '#686868',
                                            'margin-top': '6px',
                                            'margin-right': '10px'
                                        }
                                    },


                                    {
                                        xtype: 'gridpanel',
                                        itemId: 'readingTypesForValidationRuleGridPanel',
                                        store: 'ReadingTypesForRule',
                                        hidden: true,
                                        //itemId: 'reading-types-grid',
                                        //store: 'ext-empty-store',
                                        hideHeaders: true,
                                        padding: 0,
                                        scroll: 'vertical',
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
                                                            if (grid.getStore().count() === 0) {
                                                                me.updateGrid();
                                                            }
                                                        }
                                                    }
                                                ]
                                            }
                                        ],
                                        height: 220,
                                        width: 670,
                                        /*dockedItems: [
                                            {
                                                xtype: 'component',
                                                dock: 'bottom',
                                                cls: 'x-form-invalid-under',
                                                itemId: 'reading-types-grid-error',
                                                height: 52,
                                                hidden: true
                                            }
                                        ]*/
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'addReadingTypeButton',
                                        text: Uni.I18n.translate('validation.addReadingTypes', 'CFG', 'Add reading types'),
                                        action: 'addReadingTypes',
                                        margin: '0 0 0 10'
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
                                margin: '10 0 0 0',
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
                                xtype: 'label',
                                cls: 'x-form-invalid-under',
                                itemId: 'propertiesErrorLabel',
                                margin: '0 0 0 275'
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
                        ];



        this.callParent(arguments);
        this.setEdit(this.edit, this.returnLink);
    },
    updateGrid: function() {
        var me = this,
            grid = me.down('#readingTypesForValidationRuleGridPanel'),
            emptyLabel = me.down('#noReadingTypesForValidationRuleLabel');
        if (grid.getStore().count() === 0) {
            emptyLabel.show();
            grid.hide();
        } else {
            emptyLabel.hide();
            grid.show();
        }
    }
});

