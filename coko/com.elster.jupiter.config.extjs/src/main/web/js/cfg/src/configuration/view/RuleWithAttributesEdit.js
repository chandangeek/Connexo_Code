/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.configuration.view.RuleWithAttributesEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-with-attributes-edit',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Uni.property.form.Property',
        'Uni.form.field.ReadingTypeDisplay'
    ],
    application: null,
    route: undefined,
    type: null,

    initComponent: function () {
        var me = this,
            dataQualityLevelRadiogroup;

        me.content = [
            {
                xtype: 'form',
                itemId: 'rule-with-attributes-edit-form',
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 600
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'error-msg',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'info-msg',
                        text: Uni.I18n.translate('editRuleWithAttributes.infoMsg', 'CFG', 'Changed attributes override the values from rule')
                    },
                    {
                        xtype: 'textfield',
                        fieldLabel: me.type === 'validation' ? Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule') : Uni.I18n.translate('general.estimationRule', 'CFG', 'Estimation rule'),
                        itemId: 'name-field',
                        name: 'name',
                        disabled: true,
                        listeners: {
                            afterrender: function (field) {
                                field.labelEl.setOpacity(1);
                            }
                        },
                        vtype: 'validateForHtmlTags'
                    },
                    {
                        xtype: 'combobox',
                        name: me.type === 'validation' ? 'validator' : 'estimator',
                        disabled: true,
                        itemId: 'rule-type-combo',
                        fieldLabel: me.type === 'validation' ? Uni.I18n.translate('validation.validator', 'CFG', 'Validator') : Uni.I18n.translate('general.estimator', 'CFG', 'Estimator'),
                        listeners: {
                            afterrender: function (field) {
                                field.labelEl.setOpacity(1);
                            }
                        },
                        vtype: 'validateForHtmlTags'
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        itemId: 'reading-type-field',
                        value: 'readingType'
                    },
                    {
                        xtype: 'property-form',
                        padding: '5 0 0 0',
                        width: '100%',
                        itemId: 'property-form'
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'edit-form-buttons',
                        layout: 'hbox',
                        fieldLabel: ' ',
                        margin: '20 0 0 0',
                        defaultType: 'button',
                        items: [
                            {
                                itemId: 'save-button',
                                text: Uni.I18n.translate('general.save', 'CFG', 'Save'),
                                ui: 'action',
                                action: 'save',
                                handler: me.saveRuleWithAttributes.bind(me)
                            },
                            {
                                itemId: 'cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                ui: 'link',
                                action: 'cancel',
                                href: me.route.buildUrl()
                            }
                        ]
                    }
                ]
            }
        ];
        if (me.type === 'validation') {
            dataQualityLevelRadiogroup = {
                xtype: 'radiogroup',
                disabled: true,
                itemId: 'data-quality-level-radiogroup',
                vertical: true,
                columns: 1,
                fieldLabel: Uni.I18n.translate('validation.dataQualityLevel', 'CFG', 'Data quality level'),
                items: [
                    {
                        boxLabel: Uni.I18n.translate('validation.dataQualityLevelFail', 'CFG', 'Suspect'),
                        inputValue: 'Suspect',
                        name: 'dataQualityLevel'
                    },
                    {
                        boxLabel: Uni.I18n.translate('validation.dataQualityLevelWarnOnly', 'CFG', 'Informative'),
                        inputValue: 'Informative',
                        name: 'dataQualityLevel'
                    }
                ],
                listeners: {
                    afterrender: function(field) {
                        field.labelEl.setOpacity(1);
                    }
                }
            };
            me.content[0].items.splice(5, 0, dataQualityLevelRadiogroup);
        }

        me.callParent();
    },

    saveRuleWithAttributes: function() {
        var me = this,
            form = me.down('#rule-with-attributes-edit-form'),
            propertyForm = me.down('property-form'),
            errorMsg = form.down('#error-msg'),
            record,
            propertiesArray;

        Ext.suspendLayouts();
        errorMsg.hide();
        form.getForm().clearInvalid();
        Ext.resumeLayouts(true);
        form.updateRecord();
        propertyForm.updateRecord();
        record = form.getRecord();
        record.propertiesStore = propertyForm.getRecord().properties();
        propertiesArray = record.propertiesStore.data.items;
        if (propertiesArray.length) {
            Ext.Array.each(propertiesArray, function (property) {
                var propertyField = propertyForm.down('[key=' + property.get('key') + ']');
                if (propertyField && propertyField.down('uni-default-button').isDisabled()) {
                    property.getPropertyValue().set('value', '');
                }
            });
        }
        me.setLoading();
        if (!record.get('id')) {
            record.phantom = true;
            record.getProxy().appendId = false;
        }
        record.save({
            success: function () {
                me.application.fireEvent('acknowledge', Uni.I18n.translate('general.editedAttributesSaved', 'CFG', 'Edited attributes saved'));
                me.route.forward();
            },
            failure: function(record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.suspendLayouts();
                        form.getForm().markInvalid(json.errors);
                        errorMsg.show();
                        Ext.resumeLayouts(true);
                    }
                }
            },
            callback: function() {
                me.setLoading(false);
            }
        });
        record.getProxy().appendId = true;
    }
});