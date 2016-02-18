Ext.define('Mdc.view.setup.comtasks.ComtaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comtaskActionMenu',
    plain: true,
    communicationTask: undefined,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editComTask'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteComTask'
        }
    ]
});