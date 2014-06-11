Ext.define('Mdc.view.setup.logbooktype.LogbookTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbookTypePreview',
    itemId: 'logbookTypePreview',
    frame: true,
    tools: [
        {
            xtype: 'button',
            itemId: 'logbookTypePreviewActionsButton',
            text: Uni.I18n.translate('logbooktype.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: { xtype: 'logbookTypeActionMenu' }
        }
    ],
    items: [
        {
            xtype: 'form',
            itemId: 'logbookTypeDetails',
            name: 'logbookTypeDetails',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            itemId: 'logbookTypeDetailsName',
                            name: 'name',
                            fieldLabel: Uni.I18n.translate('logbooktype.name', 'MDC', 'Name')
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            itemId: 'logbookTypeDetailsObis',
                            name: 'obis',
                            fieldLabel: Uni.I18n.translate('logbooktype.obis', 'MDC', 'OBIS code')
                        }
                    ]
                }
            ]
        }
    ]
});