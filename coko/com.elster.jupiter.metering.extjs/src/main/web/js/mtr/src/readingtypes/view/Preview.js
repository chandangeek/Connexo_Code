Ext.define('Mtr.readingtypes.view.Preview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-preview',
    itemId: 'reading-types-preview',
    requires: [
        'Mtr.readingtypes.view.PreviewForm'
    ],
    frame: true,


    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('readingtypesmanagment.actions', 'MTR', 'Actions'),
            itemId: 'action-button',
            iconCls: 'x-uni-action-iconD',
            privileges : Mtr.privileges.ReadingTypes.admin,
            menu: {
                xtype: 'reading-types-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'reading-types-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});
