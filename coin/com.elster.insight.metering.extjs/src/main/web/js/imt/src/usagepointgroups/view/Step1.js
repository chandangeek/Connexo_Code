/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Step1', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepoint-group-wizard-step1',
    xtype: 'usagepoint-group-wizard-step1',
    ui: 'large',    
    isEdit: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'step1-add-usagepointgroup-errors',
                xtype: 'uni-form-error-message',
                hidden: true,
                width: 400
            }
        ];

        if (me.isEdit && !Imt.privileges.UsagePointGroup.canAdministrate() && Imt.privileges.UsagePointGroup.canAdministrateUsagePointOfEnumeratedGroup()) {
            me.items.push({
                itemId: 'usagepoint-group-name-display-field',
                xtype: 'displayfield',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name')
            });
        } else {
            me.items.push({            
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                itemId: 'usagepoint-group-name-field',
                required: true,
                allowBlank: false,
                maxLength: 80,
                enforceMaxLength: true,
                width: 400,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            });
        }

        if (me.isEdit) {
            me.items.push({
                itemId: 'usagepoint-group-type-display-field',
                xtype: 'displayfield',
                name: 'dynamic',
                fieldLabel: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('usagepointgroup.wizard.dynamic', 'IMT', 'Dynamic usage point group (based on search criteria)')
                        : Uni.I18n.translate('usagepointgroup.wizard.static', 'IMT', 'Static usage point group (based on search results)');
                }
            });
        } else {
            me.items.push({
                itemId: 'staticDynamicRadioButton',
                xtype: 'radiogroup',
                columns: 1,
                fieldLabel: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                required: true,
                vertical: true,
                items: [
                    {
                        itemId: 'dynamic-usagepoint-group',
                        boxLabel: Uni.I18n.translate('usagepointgroup.wizard.dynamic', 'IMT', 'Dynamic usage point group (based on search criteria)'),
                        name: 'dynamic',
                        inputValue: true
                    },
                    {
                        itemId: 'static-usagepoint-group',
                        boxLabel: Uni.I18n.translate('usagepointgroup.wizard.static', 'IMT', 'Static usage point group (based on search results)'),
                        name: 'dynamic',
                        inputValue: false
                    }
                ]
            });
        }

        me.callParent(arguments);
    }
});