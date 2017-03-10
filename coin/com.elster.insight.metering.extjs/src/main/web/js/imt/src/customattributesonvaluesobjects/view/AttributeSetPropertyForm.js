/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.AttributeSetPropertyForm', {
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
        'Imt.customattributesonvaluesobjects.service.ActionMenuManager',
        'Imt.customattributesonvaluesobjects.service.VersionsManager'
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

            defaults: {
                resetButtonHidden: true,
                labelWidth: 250,
                width: 600
            }
        }
    ],

    initComponent: function () {
        var me = this,
            versionsContainer;

        me.fieldLabel = me.record.get('name');
        me.callParent(arguments);
        versionsContainer = me.down('#time-sliced-versions-container');
        if (me.record.get('isVersioned')) {
            Imt.customattributesonvaluesobjects.service.VersionsManager.addVersion(me.record, versionsContainer, me.router, me.attributeSetType, me.down('property-form'));
            versionsContainer.show();
        } else {
            me.down('property-form').loadRecord(me.record);
        }
        if (me.actionMenuXtype && me.record.get('isEditable')) {
            Imt.customattributesonvaluesobjects.service.ActionMenuManager.addAction(me.actionMenuXtype, me.record, me.router, me.attributeSetType);
        }
    }
});