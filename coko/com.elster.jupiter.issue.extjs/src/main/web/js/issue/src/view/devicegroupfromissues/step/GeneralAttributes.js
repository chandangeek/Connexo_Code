/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.step.GeneralAttributes', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.general-attributes-step',

    requires: [
        'Uni.util.FormErrorMessage'
    ],

    ui: 'large',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'step-error-popup',
                width: 400,
                hidden: true
            },
            {
                xtype: 'textfield',
                itemId: 'group-name',
                name: 'groupName',
                fieldLabel: Uni.I18n.translate('devicegroupfromissues.wizard.step.generalAttributes.label.Name', 'ISU', 'Name'),
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
            },
            {
                xtype: 'displayfield',
                itemId: 'group-type',
                name: 'groupType',
                fieldLabel: Uni.I18n.translate('devicegroupfromissues.wizard.step.generalAttributes.label.Type', 'ISU', 'Type'),
                maxLength: 80,
                enforceMaxLength: true,
                width: 400,
                style: 'margin: 0px 0px 10px -97px',
                renderer: function () {
                    return Uni.I18n.translate('devicegroupfromissues.wizard.step.generalAttributes.text.typeDescription', 'ISU', 'Static device group (based on search results)');
                }
            },
            {
                xtype: 'uni-form-info-message',
                itemId: 'information-message',
                text: Uni.I18n.translate('devicegroupfromissues.wizard.step.generalAttributes.text.info', 'ISU', 'Attention: Once this group will be created, you will not be able to edit it using this process. Please use the editing functionality available for device groups via the Device page in MultiSense.'),
                margin: '0 10 5 0',
                iconCmp: {
                    xtype: 'component',
                    style: 'font-size: 22px; color: #71adc7; margin: 2px -22px 0px -22px;',
                    cls: 'icon-info'
                },
                width: 450,
                height: 65,
                style: 'border: 1px solid #71adc7; border-radius: 10px; padding: 3px 5px 5px 32px;',
            },
        ];

        me.callParent(arguments);
    }

});