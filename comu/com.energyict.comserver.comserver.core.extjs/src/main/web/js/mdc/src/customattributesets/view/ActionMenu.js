Ext.define('Mdc.customattributesets.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.custom-attribute-sets-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('customattributesets.editlevels', 'MDC', 'Edit levels'),
            itemId: 'custom-attribute-sets-edit-levels'
        }
    ]
});