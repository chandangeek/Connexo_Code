Ext.define('Imt.usagepointmanagement.view.forms.CustomPropertySetInfo', {
    extend: 'Uni.property.form.Property',
    alias: 'widget.cps-info-form',

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                itemId: 'restore-property-form-values-button',
                text: Uni.I18n.translate('general.restoreToDefault', 'IMT', 'Restore to default'),
                iconCls: 'icon-spinner12',
                iconAlign: 'left',
                handler: function () {
                    me.restoreAll();
                }
            }
        ];

        me.callParent(arguments);
    }
});