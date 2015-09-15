Ext.define('Imt.registerdata.view.GeneralPreview', {
    extend: 'Ext.panel.Panel',
    itemId: 'registerGeneralPreview',

    requires: [
        'Imt.registerdata.view.ActionMenu'
    ],

    frame: true,

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


