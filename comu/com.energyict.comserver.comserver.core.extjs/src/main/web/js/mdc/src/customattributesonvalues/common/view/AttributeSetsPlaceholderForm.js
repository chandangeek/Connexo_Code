Ext.define('Mdc.customattributesonvalues.common.view.AttributeSetsPlaceholderForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.custom-attribute-sets-placeholder-form',

    requires: [
        'Mdc.customattributesonvalues.common.view.AttributeSetPropertyForm'
    ],

    flex: 1,

    loadRecords: function(recordsArr) {
        var me = this;

        Ext.suspendLayouts();
        Ext.each(recordsArr, function (record) {
            me.add({
                xtype: 'custom-attribute-set-property-form',
                record: record,
                flex: 1
            })
        });
        Ext.resumeLayouts(true);
    }
});