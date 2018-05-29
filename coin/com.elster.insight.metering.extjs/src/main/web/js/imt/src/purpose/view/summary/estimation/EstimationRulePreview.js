/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceDataEstimationRulePreview',
    frame: true,
    requires: [
        // 'Mdc.model.EstimationRule'
    ],
    title: '',
    layout: 'form',
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
            defaults: {
                labelWidth: 250
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
    }
});