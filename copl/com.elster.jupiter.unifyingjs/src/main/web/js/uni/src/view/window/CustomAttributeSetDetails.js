Ext.define('Uni.view.window.CustomAttributeSetDetails', {
    extend: 'Ext.window.Window',
    xtype: 'custom-attribute-set-details',
    closable: true,
    width: 600,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    record: null,

    requires: [
        'Uni.view.form.CustomAttributeSetDetails'
    ],

    items: {
        xtype: 'custom-attribute-set-details-form'
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.down('custom-attribute-set-details-form').setTitle(Uni.I18n.translate('customattributeset.customTitle', 'UNI', '\'{0}\' attribute set details', [encodeURIComponent(me.record.name)]));
        me.down('custom-attribute-set-details-form').loadCustomRecord(me.record);
    }
});