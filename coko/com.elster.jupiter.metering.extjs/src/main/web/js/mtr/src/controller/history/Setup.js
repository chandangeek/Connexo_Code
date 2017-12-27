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
                //readingtypegroups: { // lori set
                readingtypes1: {
                    // title: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'), //lori set
                    title: Uni.I18n.translate('readingtypes.readingTypes1', 'MTR', 'Reading types'),
                    route: 'readingtypes1',
                    // route: 'readingtypegroups', // lori set
                    controller: 'Mtr.controller.readingtypesgroup.ReadingTypesGroup',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            //title: Uni.I18n.translate('readingtypes.readingTypeGroups.add', 'MTR', 'Add reading type group'), // lori set
                            title: Uni.I18n.translate('readingtypes.readingTypes1.add', 'MTR', 'Add reading type'),
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
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type group XYZ'),
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
                        // {
                        //     // adaug partea de edit  -- lori
                        // },
                        readingtypes: {
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type group XYZ'),
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


