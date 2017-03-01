/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ReadingEstimationWithRuleWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-estimation-with-rule-window',
    modal: true,
    title: Uni.I18n.translate('general.selectEstimationRule', 'IMT', 'Select estimation rule'),
    bothSuspected: false,
    record: null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-estimation-window-form',
            padding: 0,
            defaults: {
                width: 503,
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'combobox',
                    itemId: 'estimation-rule-field',
                    name: 'estimationRule',
                    fieldLabel: Uni.I18n.translate('estimationDevice.estimation rule', 'IMT', 'Estimation rule'),
                    required: true,
                    editable: 'false',
                    store: 'Imt.purpose.store.EstimationRules',
                    valueField: 'id',
                    displayField: 'displayName',
                    queryMode: 'local',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('general.selectAnEstimationRule', 'IMT', 'Select an estimation rule...'),
                    listeners: {
                        change: {
                            fn: function (implementationCombo, newValue) {
                                var estimator = implementationCombo.getStore().getById(newValue);

                                estimator && me.down('property-form').loadRecord(estimator);
                                me.updateLayout();
                                me.center();
                            }
                        }
                    }
                },
                {
                    xtype: 'property-form',
                    itemId: 'property-form',
                    isEdit: false,
                    isReadOnly: true,
                    defaults: {
                        labelWidth: 200
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'estimate-reading-button',
                            text: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    getEstimator: function(){
        // var me = this;
        // return me.down('#estimation-rule-field').getStore().getById(me.down('#estimation-rule-field').getValue())
    }
});