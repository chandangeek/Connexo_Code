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
                readingtypes: {
                    title: Uni.I18n.translate('readingtypes.readingTypes.breadcrumbs', 'MTR', 'Reading types'),
                    route: 'readingtypes',
                    controller: 'Mtr.controller.ReadingTypesGroup',
                    privileges : Mtr.privileges.ReadingTypes.view,
                    action: 'showOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('readingtypes.readingTypes.add', 'MTR', 'Add reading type'),
                            route: 'add',
                            controller: 'Mtr.controller.AddReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview'
                        },
                        bulk: {
                            title: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action'),
                            route: '{aliasName}/bulk',
                            controller: 'Mtr.controller.BulkAction',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showOverview',
                            callback: function (route) {
                                this.getApplication().on('groupdetailsloaded', function (readingTypeGroupName) {
                                    route.setTitle(readingTypeGroupName);
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        view: {
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type'),
                            route: '{aliasName}/view',
                            controller: 'Mtr.controller.ReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showReadingTypesGroupDetails',
                            callback: function (route) {
                                this.getApplication().on('groupdetailsloaded', function (readingTypeGroupName) {
                                    route.setTitle(readingTypeGroupName);
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        readingtypes: {
                            title: Uni.I18n.translate('readingtypegroups.readingtypegroup', 'MTR', 'Reading type'),
                            route: '{aliasName}/readingtypes',
                            controller: 'Mtr.controller.ReadingTypesGroup',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'showReadingTypesInGroup',
                            items: {
                                add: {  // add reading type in reading types {aliasName}
                                    title: Uni.I18n.translate('readingtypes.readingTypes.add', 'MTR', 'Add reading type'),
                                    route: 'add',
                                    controller: 'Mtr.controller.AddReadingTypesGroup',
                                    privileges: Mtr.privileges.ReadingTypes.admin,
                                    action: 'showOverview'
                                }
                            },

                            callback: function (route) {
                                this.getApplication().on('groupdetailsloaded', function (readingTypeGroupName) {
                                    route.setTitle(readingTypeGroupName);
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                }
            }
        }
    }
});


