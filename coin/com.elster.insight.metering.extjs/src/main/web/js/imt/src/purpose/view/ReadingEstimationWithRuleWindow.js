/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ReadingEstimationWithRuleWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-estimation-with-rule-window',
    modal: true,
    title: Uni.I18n.translate('general.estimateValueWithRule', 'IMT', 'Estimate with rule'),
    bothSuspected: false,
    record: null,
    hasRules: false,

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
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('estimationDevice.estimation rule', 'IMT', 'Estimation rule'),
                    itemId: 'estimator-container',
                    required: true,
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'estimation-rule-field',
                            width: 280,
                            name: 'estimationRule',
                            editable: false,
                            store: 'Imt.purpose.store.EstimationRules',
                            valueField: 'id',
                            displayField: 'name',
                            queryMode: 'local',
                            forceSelection: true,
                            hidden: !me.hasRules,
                            emptyText: Uni.I18n.translate('general.selectAnEstimationRule', 'IMT', 'Select an estimation rule...'),
                            listeners: {
                                change: {
                                    fn: function (implementationCombo, newValue) {
                                        var estimator = implementationCombo.getStore().getById(newValue),
                                            hasEmptyRequiredProperties;

                                        if (estimator) {
                                            me.down('property-form').loadRecord(estimator);
                                            hasEmptyRequiredProperties = estimator.properties().getRange().find(function(property) {
                                                return property.get('required') && Ext.isEmpty(property.get('value'));
                                            });
                                        }

                                        me.updateLayout();
                                        me.center();
                                        me.down('#estimate-reading-button').setDisabled(!!hasEmptyRequiredProperties);
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('noEstimationRules.message', 'IMT', 'No applicable estimation rules'),
                            itemId: 'no-estimation-rules-component',
                            hidden: me.hasRules,
                            style: {
                                'color': '#FF0000',
                                'margin': '6px 0px 6px 0px'
                            }
                        }
                    ]
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
        var me = this;
        return me.down('#estimation-rule-field')
            .getStore()
            .getById(me.down('#estimation-rule-field').getValue())
            .get('estimatorImpl');
    }
});