Ext.define('Imt.registerdata.view.GeneralPreview', {
    extend: 'Ext.panel.Panel',
    itemId: 'registerGeneralPreview',

    requires: [
        'Imt.registerdata.view.ActionMenu'
    ],

    frame: true,

    tools: [
//        {
//            xtype: 'button',
//            text: Uni.I18n.translate('general.actions', 'IMT', Uni.I18n.translate('general.actions', 'IMT', 'Actions')),
//            iconCls: 'x-uni-action-iconD',
//            itemId: 'registerActionMenu',
//            menu: {
//                xtype: 'registerActionMenu'
//            }
//        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


