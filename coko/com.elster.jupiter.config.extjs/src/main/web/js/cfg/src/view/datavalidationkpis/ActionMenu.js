Ext.define('Cfg.view.datavalidationkpis.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.cfg-data-validation-kpis-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            itemId: 'remove-data-validation-kpi',
            action: 'remove'
        }
    ]
});
