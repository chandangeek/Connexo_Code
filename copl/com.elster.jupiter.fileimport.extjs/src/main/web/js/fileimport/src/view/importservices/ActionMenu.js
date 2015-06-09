Ext.define('Fim.view.importservices.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.fim-import-service-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-import-service',
            text: Uni.I18n.translate('general.edit', 'FIM', 'Edit'),
            privileges: Fim.privileges.DataImport.getAdmin,
            action: 'editImportService'
        },
        {
            itemId: 'remove-import-service',
            text: Uni.I18n.translate('general.remove', 'FIM', 'Remove'),
            privileges: Fim.privileges.DataImport.getAdmin,
            action: 'removeImportService'
        },
        {
            itemId: 'view-import-service',
            text: Uni.I18n.translate('general.viewDetails', 'FIM', 'View details'),
            action: 'viewImportService'
        },
		{
            itemId: 'view-history-import-service',
            text: Uni.I18n.translate('general.viewHistory', 'FIM', 'View history'),
            action: 'viewImportServiceHistory'
        },
        {
            itemId: 'activate-import-service',
            text: Uni.I18n.translate('general.activate', 'FIM', 'Activate'),
            privileges: Fim.privileges.DataImport.getAdmin,
            action: 'activateimportservice'
        },
        {
            itemId: 'deactivate-import-service',
            text: Uni.I18n.translate('general.deactivate', 'FIM', 'Deactivate'),
            privileges: Fim.privileges.DataImport.getAdmin,
            action: 'deactivateimportservice'
        }
    ]
});