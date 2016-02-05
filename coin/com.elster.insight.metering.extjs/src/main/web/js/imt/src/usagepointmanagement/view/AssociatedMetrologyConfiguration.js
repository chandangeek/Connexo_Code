Ext.define('Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.associated-metrology-configuration',
    itemId: 'associated-metrology-configuration',

    requires: [
        'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.usagepointmanagement.view.SetupActionMenu'
    ],

    //title: Uni.I18n.translate('usagepoint.linked-metrologyconfiguration', 'IMT', 'Associated Metrology configuration'),
    router: null,
    //ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 175,
                //fieldLabel: Uni.I18n.translate('usagepoint.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'associatedMetrologyConfiguration',
                items: [

                ]
            },
            {
                xtype: 'custom-attribute-sets-placeholder-form',
                inline: true,
                itemId: 'metrology-custom-attribute-sets-placeholder-form-id',
                actionMenuXtype: 'usage-point-setup-action-menu',
                attributeSetType: 'up',
                router: me.router
            }
        ];
        me.callParent(arguments);
    }
});