Ext.define('Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.custom-attribute-sets-placeholder-form',

    requires: [
        'Imt.customattributesonvaluesobjects.view.AttributeSetPropertyForm',
        'Imt.customattributesonvaluesobjects.service.ActionMenuManager'
    ],

    flex: 1,
    actionMenuXtype: null,
    router: null,

    loadStore: function(store) {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll();
        if (me.actionMenuXtype) {
            Imt.customattributesonvaluesobjects.service.ActionMenuManager.removePrevious(me.actionMenuXtype);
        }
        store.each(function (record) {
            me.add({
                xtype: 'custom-attribute-set-property-form',
                record: record,
                router: me.router,
                attributeSetType: me.attributeSetType,
                actionMenuXtype: me.actionMenuXtype,
                flex: 1
            })
        });
        Ext.resumeLayouts(true);
    }
});