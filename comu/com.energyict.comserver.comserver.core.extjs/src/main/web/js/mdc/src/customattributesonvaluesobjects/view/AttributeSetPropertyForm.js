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
        'Mdc.customattributesonvaluesobjects.service.ActionMenuManager'
    ],


    initComponent: function() {
        var me = this;

        me.items = [
            {
                xtype: 'property-form',
                isEdit: false,

                defaults: {
                    resetButtonHidden: true,
                    labelWidth: 250,
                    width: 600
                }
            }
        ];

        me.fieldLabel = me.record.get('name');

        me.callParent(arguments);
        me.down('property-form').loadRecord(me.record);
        if (me.actionMenuXtype && me.record.get('editable')) {
            Mdc.customattributesonvaluesobjects.service.ActionMenuManager.addAction(me.actionMenuXtype, me.record, me.router, me.attributeSetType);
        }
    }
});