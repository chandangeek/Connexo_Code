Ext.define('Imt.usagepointmanagement.view.forms.attributes.CustomAttributeSetForm', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    requires: [
        'Uni.property.form.Property'
    ],
    alias: 'widget.custom-attribute-set-form',

    initComponent: function () {
        var me = this;

        me.viewForm = {
            xtype: 'property-form',
            itemId: 'view-form',
            isEdit: false,
            defaults: {
                labelWidth: me.viewDefaults.labelWidth
            }
        };

        me.editForm = {
            xtype: 'property-form',
            itemId: 'edit-form',
            defaults: {
                labelWidth: me.editDefaults.labelWidth,
                width: 128
            }
        };

        me.callParent();
    }
});