Ext.define('Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.custom-attribute-sets-placeholder-form',

    requires: [
        'Imt.customattributesonvaluesobjects.view.AttributeSetPropertyForm',
        'Imt.customattributesonvaluesobjects.view.InlineEditableSetPropertyForm',
        'Imt.customattributesonvaluesobjects.service.ActionMenuManager'
    ],

    flex: 1,
    parent: null,
    actionMenuXtype: null,
    router: null,
    inline: null,

    loadStore: function(store) {
        var me = this,
            form;

        form = me.inline ? 'inline-editable-set-property-form' :'custom-attribute-set-property-form';

        Ext.suspendLayouts();
        me.removeAll();
        if (me.actionMenuXtype) {
            Imt.customattributesonvaluesobjects.service.ActionMenuManager.removePrevious(me.actionMenuXtype);
        }
        store.each(function (record) {
            me.add({
                xtype: form,
                record: record,
                parent: me.parent,
                router: me.router,
                attributeSetType: me.attributeSetType,
                actionMenuXtype: me.actionMenuXtype,
                flex: 1
            })
        });
        Ext.resumeLayouts(true);
    }
});