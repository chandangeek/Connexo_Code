Ext.define('Mdc.view.setup.devicelogbooks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLogbooksActionMenu',
    itemId: 'deviceLogbooksActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    record: null,
    items: [
        {
            itemId: 'editLogbook',
            text: Uni.I18n.translate('general.changeNextReadingBlockStart', 'MDC', 'Change next reading block start'),
            action: 'editLogbook'
        }
    ]
});
