/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',
    requires:[],

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'MTR', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                readingtypegroups: {   //lori
                    title: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'),
                    route: 'readingtypegroups',
                    controller: 'Mtr.readingtypes.controller.ReadingTypes',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showGroupsOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('readingtypegroups.add', 'MTR', 'Add reading types'),
                            route: 'add',
                            controller: 'Mtr.readingtypes.controller.AddReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
                            route: 'bulk',
                            controller: 'Mtr.readingtypes.controller.GroupBulkAction',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showGroupsOverview'
                        }
                    }
                },
                readingtypes: {
                    title: Uni.I18n.translate('readingtypes.title', 'MTR', 'Reading types'),
                    route: 'readingtypes',
                    controller: 'Mtr.readingtypes.controller.ReadingTypes',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('readingtypes.add', 'MTR', 'Add reading types'),
                            route: 'add',
                            controller: 'Mtr.readingtypes.controller.AddReadingTypes',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
                            route: 'bulk',
                            controller: 'Mtr.readingtypes.controller.BulkAction',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});


