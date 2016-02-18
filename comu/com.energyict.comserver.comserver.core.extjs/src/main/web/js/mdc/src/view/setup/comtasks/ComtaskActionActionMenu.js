Ext.define('Mdc.view.setup.comtasks.ComtaskActionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comtaskActionActionMenu',
    plain: true,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editComTaskAction'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteComTaskAction'
        }
    ]
});