/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.define-metrology-configuration-wizard',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormEmptyMessage',
        'Uni.form.field.DateTime',
        'Imt.metrologyconfiguration.view.PurposesField'
    ],

    layout: {
        type: 'card',
        deferredRender: true
    },

    router: null,
    returnLink: null,
    isPossibleAdd: true,
    createTime: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'define-metrology-configuration-step1',
                title: Uni.I18n.translate('metrologyConfiguration.wizard.step1title', 'IMT', 'Step 1: Select metrology configuration'),
                isWizardStep: true,
                navigationIndex: 1,
                ui: 'large',
                defaults: {
                    labelWidth: 230,
                    width: 567
                },
                items: [
                    me.isPossibleAdd ?
                    {
                        itemId: 'general-info-warning',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    } :
                    {
                        itemId: 'not-possible-add',
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('metrologyConfiguration.wizard.notPossibleLink', 'IMT', "You can't link a metrology configuration to this usage point because there are no matching metrology configurations found or you have no privileges to see them. Please check your metrology configurations attributes.")
                    },
                    me.isPossibleAdd ?
                    {
                        xtype: 'combobox',
                        name: 'id',
                        itemId: 'metrology-configuration-combo',
                        fieldLabel: Uni.I18n.translate('general.label.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                        afterSubTpl: '<span class="field-additional-info" style="color: #686868; font-style: italic">'
                        + Uni.I18n.translate('metrologyConfiguration.wizard.clarification', 'IMT', 'The metrology configurations applicable to the usage point.')
                        + '</span>',
                        required: true,
                        store: 'Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations',
                        displayField: 'name',
                        valueField: 'id',
                        queryMode: 'local',
                        forceSelection: true,
                        emptyText: Uni.I18n.translate('metrologyConfiguration.wizard.emptyText', 'IMT', 'Select metrology configuration...'),
                        listeners: {
                            errorchange: {
                                fn: function (field, error) {
                                    if (field.rendered) {
                                        field.getEl().down('.field-additional-info').setDisplayed(Ext.isEmpty(error));
                                    }
                                }
                            }
                        }
                    } :
                    {
                        xtype: 'displayfield',
                        itemId: 'up-service-category-displayfield',
                        fieldLabel: Uni.I18n.translate('general.label.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                        required: true,
                        htmlEncode: false,
                        style: 'font-style: italic',
                        value: '<span style="color: #686868; font-style: italic">'
                        + Uni.I18n.translate('metrologyConfiguration.wizard.noAvailable', 'IMT', 'No available metrology configurations')
                        + '</span>'
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'metrology-configuration-start-date',
                        hidden: true,
                        required: true,
                        fieldLabel: Uni.I18n.translate('metrologyConfiguration.wizard.startDate', 'IMT', 'Start date'),
                        defaults: {
                            width: '100%'
                        },
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'metrology-configuration-start-date-field',
                                value: me.createTime || new Date(),
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'custom-attributes-radiogroup',
                        fieldLabel: Uni.I18n.translate('metrologyConfiguration.wizard.customAttributes', 'IMT', 'Custom attributes'),
                        required: true,
                        hidden: true,
                        width: 700,
                        layout: 'vbox',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('metrologyConfiguration.wizard.custtomAttributes.create', 'IMT', 'Create new versions for versioned custom attribute sets'),
                                name: 'customAttributes',
                                inputValue: false,
                                checked: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('metrologyConfiguration.wizard.custtomAttributes.override', 'IMT', 'Override values of existing versions of versioned custom attribute sets'),
                                name: 'customAttributes',
                                inputValue: true
                            }
                        ]
                    },
                    {
                        xtype: 'purposes-field',
                        itemId: 'purposes-field'
                    }
                ]
            }
        ];

        me.bbar = {
            itemId: 'define-metrology-configuration-wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true,
                    hidden: !me.isPossibleAdd
                },
                {
                    itemId: 'nextButton',
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'addButton',
                    text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                    ui: 'action',
                    action: 'add',
                    hidden: !me.isPossibleAdd
                },
                {
                    itemId: 'wizardCancelButton',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    href: me.returnLink
                }
            ]
        };

        me.callParent(arguments);
    },

    updateRecord: function (record) {
        var me = this,
            step = me.getLayout().getActiveItem();
        switch (step.navigationIndex) {
            case 1:
                var combo = step.down('#metrology-configuration-combo');
                me.getRecord().set('id',combo.getValue());
                me.getRecord().set('name',combo.getRawValue());
                me.getRecord().set('purposes', step.down('#purposes-field').getValue());
                me.getRecord().set('activationTime', step.down('#metrology-configuration-start-date-field').getValue());
                record && me.getRecord().set('version', record.get('version'));
                me.callParent(arguments);
                break;
            default:
                step.updateRecord();
                me.getRecord().customPropertySets().add(step.getRecord());
        }
    },

    markInvalid: function (errors) {
        this.toggleValidation(errors);
    },

    clearInvalid: function () {
        this.toggleValidation();
    },

    toggleValidation: function (errors) {
        var me = this,
            isValid = !errors,
            step = me.getLayout().getActiveItem(),
            warning = step.down('uni-form-error-message');

        Ext.suspendLayouts();
        if (warning) {
            warning.setVisible(!isValid);
        }
        if (step.xtype === 'cps-info-form') {
            if (!isValid) {
                step.markInvalid(errors);
            } else {
                step.clearInvalid();
            }
        } else {
            if (!isValid) {
                step.getForm().markInvalid(errors);
            } else {
                step.getForm().clearInvalid();
            }
        }
        Ext.resumeLayouts(true);
    }
});