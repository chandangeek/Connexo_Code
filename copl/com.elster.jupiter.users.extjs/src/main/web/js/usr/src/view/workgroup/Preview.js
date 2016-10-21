Ext.define('Usr.view.workgroup.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usr-workgroup-preview',
    requires: [
        'Usr.view.workgroup.PreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'btn-workgroup-preview-action-menu',
            menu: {
                xtype: 'usr-workgroup-action-menu'
            }
        }
    ],
    items: {
        xtype: 'usr-workgroup-preview-form',
        itemId: 'pnl-workgroup-preview-form'
    }
});
