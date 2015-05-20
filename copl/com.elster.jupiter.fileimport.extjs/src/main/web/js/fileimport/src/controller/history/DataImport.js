Ext.define('Fim.controller.history.DataImport', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Fim.privileges.DataImport'
    ],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {                
				importservices: {                    
					title: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),                    
					route: 'importservices',                    
					controller: 'Fim.controller.ImportServices',
                    privileges : Fim.privileges.DataImport.canView,
                    action: 'showImportServices',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addImportService', 'FIM', 'Add import service'),
                            route: 'add',
                            controller: 'Fim.controller.ImportServices',
                            privileges: Fim.privileges.DataImport.admin,
                            action: 'showAddImportService'
                        },
                        importservice: {
                            title: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                            route: '{importServiceId}',
                            controller: 'Fim.controller.ImportServices',
                            action: 'showImportService',
                            callback: function (route) {
                                this.getApplication().on('importserviceload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'FIM', 'Edit'),
                                    route: 'edit',
                                    controller: 'Fim.controller.ImportServices',
                                    privileges: Fim.privileges.DataImport.update,
                                    action: 'showEditImportService'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
