Ext.define('Fim.view.importServices.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-import-service-preview',
    requires: [
        'Fim.view.importServices.PreviewForm'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'FIM', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'fim-import-service-action-menu'
            }
        }
    ],
    items: {
        xtype: 'fim-import-service-preview-form',
        itemId: 'pnl-import-service-preview-form'
    }
});
