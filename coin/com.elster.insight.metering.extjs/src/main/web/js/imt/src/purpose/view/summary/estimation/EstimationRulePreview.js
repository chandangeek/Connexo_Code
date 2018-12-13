/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.estimationCfgRulePreview',
    itemId: 'estimationCfgRulePreview',
    frame: true,
    requires: [
        'Imt.purpose.model.EstimationRule'
    ],
    title: '',
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            name: 'name',
            itemId: 'estimation-rule-field',
            fieldLabel: Uni.I18n.translate('estimationPurpose.estimationRule', 'IMT', 'Estimation rule')
        },
        {
            name: 'displayName',
            itemId: 'estimator-field',
            fieldLabel: Uni.I18n.translate('estimationPurpose.estimator', 'IMT', 'Estimator')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
            itemId: 'status-field',
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.active', 'IMT', 'Active')
                } else {
                    return Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                }
            }
        },
        {
            xtype: 'container',
            itemId: 'readingTypesArea',
            items: []
        },
        {
            xtype: 'property-form',
            itemId: 'rule-property-form',
            isEdit: false,
            layout: 'vbox',
            defaults: {
                labelWidth: 260,
                width: 500
            }
        },
        {
            itemId: 'estimation-comment-field',
            name: 'commentValue',
            fieldLabel: Uni.I18n.translate('general.estimationComment', 'IMT', 'Estimation comment'),
            renderer: function (string) {
                return string ? string : '-';
            }
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    },

    updateEstimationPreview: function (estimationRule) {

        var me = this,
            readingTypes = estimationRule.data.readingTypes;

        Ext.suspendLayouts();
        me.loadRecord(estimationRule);
        me.setTitle(Ext.String.htmlEncode(estimationRule.get('name')));
        me.down('property-form').loadRecord(estimationRule);
        me.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('general.readingTypes', 'IMT', 'Reading types'),
                readingType = readingTypes[i];

            me.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 250,
                            value: readingType
                        }
                    ]
                }
            );
        }
        Ext.resumeLayouts(true);

    },

});