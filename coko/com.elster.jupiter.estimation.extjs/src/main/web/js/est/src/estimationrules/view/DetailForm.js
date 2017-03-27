/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.property.form.Property',
        'Est.estimationrules.view.ActionMenu'
    ],
    alias: 'widget.estimation-rules-detail-form',
    title: Uni.I18n.translate('general.details', 'EST', 'Details'),
    layout: 'form',
    actionMenuItemId: null,
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            itemId: 'name-field',
            fieldLabel: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
            name: 'name'
        },
        {
            itemId: 'estimator-field',
            fieldLabel: Uni.I18n.translate('estimationrules.estimator', 'EST', 'Estimator'),
            name: 'displayName'
        },
        {
            itemId: 'status-field',
            fieldLabel: Uni.I18n.translate('general.status', 'EST', 'Status'),
            name: 'active',
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.active', 'EST', 'Active') : Uni.I18n.translate('general.inactive', 'EST', 'Inactive');
            }
        },
        {
            xtype: 'property-form',
            itemId: 'estimation-rule-properties',
            isEdit: false,
            defaults: {
                labelWidth: 250
            }
        },
        {
            fieldLabel: Uni.I18n.translate('general.markAsProjected', 'EST', 'Mark as projected'),
            itemId: 'mark-projected-field',
            name: 'markProjected',
            hidden: true,
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.yes', 'EST', 'Yes') : Uni.I18n.translate('general.no', 'EST', 'No');
            }
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'reading-types-field',
            fieldLabel: Uni.I18n.translate('general.readingTypes', 'EST', 'Reading types'),
            defaults: {
                xtype: 'reading-type-displayfield',
                fieldLabel: undefined
            }
        }
    ],

    MULTISENSE_KEY: 'MultiSense',
    INSIGHT_KEY: 'MdmApp',

    updateForm: function (record) {
        var me = this,
            readingTypesField = me.down('#reading-types-field'),
            appName = Uni.util.Application.getAppName();

        if(appName === me.MULTISENSE_KEY) {
            me.down('#mark-projected-field').hide();
        } else if (appName === me.INSIGHT_KEY) {
            me.down('#mark-projected-field').show();
        }
        Ext.suspendLayouts();
        if (!me.staticTitle) {
            me.setTitle(Ext.String.htmlEncode(record.get('name')));
        }
        me.loadRecord(record);
        readingTypesField.removeAll();
        Ext.Array.each(record.get('readingTypes'), function (item) {
            readingTypesField.add({
                value: item
            });
        });
        me.down('property-form').loadRecord(record);
        Ext.resumeLayouts(true);
    },
    initComponent: function () {
        var me = this;

        if(!me.noActionsButton){
            me.tools = [
                {
                    xtype: 'uni-button-action',
                    itemId: 'estimation-rules-detail-action-menu-button',
                    privileges: Est.privileges.EstimationConfiguration.administrate,
                    menu: {
                        xtype: 'estimation-rules-action-menu',
                        itemId: me.actionMenuItemId
                    }
                }
            ];
        }

        me.callParent(arguments);
    }
});