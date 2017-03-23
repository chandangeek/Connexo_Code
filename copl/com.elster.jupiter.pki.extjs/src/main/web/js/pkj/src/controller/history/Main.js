/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.history.Main', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'PKJ', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                truststores: {
                    title: Uni.I18n.translate('general.trustStores', 'PKJ', 'Trust stores'),
                    //privileges: Pkj.privileges.TrustStore.view,
                    route: 'truststores',
                    controller: 'Pkj.controller.TrustStores',
                    action: 'showTrustStores',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addTrustStore', 'PKJ', 'Add trust store'),
                            //privileges: Wss.privileges.Webservices.admin,
                            route: 'add',
                            controller: 'Pkj.controller.TrustStores',
                            action: 'showAddTrustStore'
                        },
                        view: {
                            route: '{trustStoreId}',
                            //privileges: Pkj.privileges.view,
                            title: Uni.I18n.translate('general.trustedCertificates', 'PKJ', 'Trusted certificates'),
                            controller: 'Pkj.controller.TrustStores',
                            action: 'showTrustedCertificates',
                            callback: function (route) {
                                this.getApplication().on('trustStoreLoaded', function (name) {
                                    route.setTitle(name);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    route: 'edit',
                                    title: Uni.I18n.translate('general.Edit', 'PKJ', 'Edit'),
                                    //privileges: Wss.privileges.Webservices.admin,
                                    controller: 'Pkj.controller.TrustStores',
                                    action: 'showEditTrustStore',
                                    callback: function (route) {
                                        this.getApplication().on('trustStoreLoaded', function (name) {
                                            route.setTitle(Ext.String.format(Uni.I18n.translate('general.editX', 'PKJ', "Edit '{0}'"), name));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                importcertificates: {
                                    route: 'importcertificates',
                                    title: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                                    controller: 'Pkj.controller.TrustStores',
                                    action: 'showImportCertificatesPage'
                                }
                            }
                        }

                    }
                },
                certificates: {
                    title: Uni.I18n.translate('general.certificates', 'PKJ', 'Certificates'),
                    //privileges: Pkj.privileges.TrustStore.view,
                    route: 'certificates',
                    controller: 'Pkj.controller.Certificates',
                    action: 'showCertificates'
                }
            }
        }
    }
});
