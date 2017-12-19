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
                readingtypegroups: {
                    title: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'),
                    route: 'readingtypegroups',
                    controller: 'Mtr.controller.readingtypesgroup.ReadingTypesGroup',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('readingtypes.readingTypeGroups.add', 'MTR', 'Add reading types'),
                            route: 'add',
                            controller: 'Mtr.controller.readingtypesgroup.AddReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
                            route: 'bulk',
                            controller: 'Mtr.controller.readingtypesgroup.GroupBulkAction',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        view: {
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type group'),
                            route: '{aliasName}/view',
                            controller: 'Mtr.controller.readingtypesgroup.ReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showReadingTypesGroupDetails',
                            callback: function (route) {
                                this.getApplication().on('readingtypesgroupload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        readingtypes: {
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type group'),
                            route: '{aliasName}/readingtypes',
                            controller: 'Mtr.controller.readingtypesgroup.ReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showReadingTypesInGroup',
                            callback: function (route) {
                                this.getApplication().on('readingtypesingroupload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                },
                readingtypes: {
                    title: Uni.I18n.translate('readingtypes.title', 'MTR', 'Reading types'),
                    route: 'readingtypes',
                    controller: 'Mtr.controller.readingtypes.ReadingTypes',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('readingtypes.add', 'MTR', 'Add reading types'),
                            route: 'add',
                            controller: 'Mtr.controller.readingtypes.AddReadingTypes',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
                            route: 'bulk',
                            controller: 'Mtr.controller.readingtypes.BulkAction',
                            privileges : Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});


