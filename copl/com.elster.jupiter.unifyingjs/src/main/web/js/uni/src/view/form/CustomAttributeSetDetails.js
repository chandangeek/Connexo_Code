/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.form.CustomAttributeSetDetails', {
    extend: 'Ext.form.Panel',
    alias: 'widget.custom-attribute-set-details-form',
    itemId: 'custom-attribute-set-details-form-id',
    ui: 'medium',
    width: '100%',
    showDefaultTitle: true,
    defaults: {
        labelWidth: 150,
        xtype: 'displayfield'
    },
    requires: [
        'Uni.util.LevelMap'
    ],
    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.name', 'UNI', 'Name')
        },
        {
            fieldLabel: Uni.I18n.translate('customattributesets.timeSliced', 'UNI', 'Time-sliced'),
            name: 'isVersioned',
            renderer: function (value) {
                return value ?
                    Uni.I18n.translate('general.yes', 'UNI', 'Yes') :
                    Uni.I18n.translate('general.no', 'UNI', 'No');
            }
        },
        {
            fieldLabel: Uni.I18n.translate('customattributesets.viewlevels', 'UNI', 'View levels'),
            name: 'viewPrivileges',
            renderer: function (value) {
                if (!Ext.isEmpty(value)) {
                    return Uni.util.LevelMap.getPrivilegesString(value);
                } else {
                    return null;
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('customattributesets.editlevels', 'UNI', 'Edit levels'),
            name: 'editPrivileges',
            renderer: function (value) {
                if (!Ext.isEmpty(value)) {
                    return Uni.util.LevelMap.getPrivilegesString(value);
                } else {
                    return null;
                }
            }
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'attributes-fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.attributes', 'UNI', 'Attributes')
        }
    ],

    initComponent: function() {
        var me = this;

      if (me.showDefaultTitle) {
          me.title = Uni.I18n.translate('customattributeset.title', 'UNI', 'Attribute set details')
      }
      this.callParent(arguments);
    },

    loadCustomRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.getForm().setValues(record);
        me.down('#attributes-fieldcontainer').removeAll();
        Ext.each(record.attributes, function (attribute) {
            var requiredIcon = '';

            if (attribute.required) {
                requiredIcon = '<span class="uni-form-item-label-required" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: left;" data-qtip="' + Uni.I18n.translate('general.required', 'UNI', 'Required') + '"></span>';
            }

            me.down('#attributes-fieldcontainer').add({
                xtype: 'displayfield',
                htmlEncode: false,
                value: '<span style="display: inline-block; float: left">' + Ext.String.htmlEncode(attribute.name) + '</span>' + requiredIcon
            });
        });
        Ext.resumeLayouts(true);
    }
});