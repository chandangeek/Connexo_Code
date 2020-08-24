/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.AttributeSetPropertyForm', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.custom-attribute-set-property-form',

    router: null,
    record: null,
    actionMenuXtype: null,
    attributeSetType: null,
    labelAlign: 'top',
    layout: 'vbox',

    requires: [
        'Uni.property.form.Property',
        'Mdc.customattributesonvaluesobjects.service.ActionMenuManager',
        'Mdc.customattributesonvaluesobjects.service.VersionsManager'
    ],

    items: [
        {
            xtype: 'container',
            itemId: 'time-sliced-versions-container',
            layout: 'hbox',
            hidden: true,
            margin: '0 0 30 0'
        },
        {
            xtype: 'property-form',
            isEdit: false,
            width: 400,

            defaults: {
                resetButtonHidden: true,
                labelWidth: 200
            }
        }
    ],

    initComponent: function () {
        var me = this,
            versionsContainer;

        me.fieldLabel = me.record.get('name');
        me.callParent(arguments);
        versionsContainer = me.down('#time-sliced-versions-container');
        if (me.record.get('timesliced')) {
            var propertiesForm = me.down('property-form');
            Mdc.customattributesonvaluesobjects.service.VersionsManager.addVersion(me.record, versionsContainer, me.router, me.attributeSetType, propertiesForm);
            versionsContainer.show();
            var textFields = propertiesForm.query('displayfield');
            Ext.Array.each(textFields, function(textfield){
                textfield.setFieldStyle({'word-break' : 'break-all', 'line-height': '28px', 'margin-top':'0px' })
            })
        } else {
            me.down('property-form').loadRecord(me.record);

        }
        if (me.actionMenuXtype && me.record.get('editable')) {
            Mdc.customattributesonvaluesobjects.service.ActionMenuManager.addAction(me.actionMenuXtype, me.record, me.router, me.attributeSetType);
        }
    }
});