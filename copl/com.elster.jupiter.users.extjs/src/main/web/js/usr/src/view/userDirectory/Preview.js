Ext.define('Usr.view.userDirectory.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usr-user-directory-preview',
    requires: [
        'Usr.view.userDirectory.PreviewForm'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'USR', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'usr-user-directory-action-menu'
            }
        }
    ],
    items: {
        xtype: 'usr-user-directory-preview-form',
        itemId: 'pnl-user-directory-preview-form'
    }
});
