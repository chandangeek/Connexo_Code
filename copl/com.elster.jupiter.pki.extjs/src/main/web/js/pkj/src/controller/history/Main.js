/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.history.Main', {
    extend: 'Uni.controller.history.Converter',

    requires:[
        'Pkj.privileges.CertificateManagement'
    ],
    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'PKJ', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                truststores: {
                    title: Uni.I18n.translate('general.trustStores', 'PKJ', 'Trust stores'),
                    privileges: Pkj.privileges.CertificateManagement.view,
                    route: 'truststores',
                    controller: 'Pkj.controller.TrustStores',
                    action: 'showTrustStores',
                    items: {
                        add: {
                            route: 'add',
                            title: Uni.I18n.translate('general.addTrustStore', 'PKJ', 'Add trust store'),
                            privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                            controller: 'Pkj.controller.TrustStores',
                            action: 'showAddTrustStore'
                        },
                        view: {
                            route: '{trustStoreId}',
                            privileges: Pkj.privileges.CertificateManagement.view,
                            title: Uni.I18n.translate('general.trustedCertificates', 'PKJ', 'Trusted certificates'),
                            controller: 'Pkj.controller.TrustStores',
                            action: 'showTrustedStoreAndCertificates',
                            callback: function (route) {
                                this.getApplication().on('trustStoreLoaded', function (name) {
                                    route.setTitle(name);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                importcertificates: {
                                    route: 'importcertificates',
                                    title: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                                    privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                                    controller: 'Pkj.controller.TrustStores',
                                    action: 'showImportCertificatesPage'
                                }
                            }
                        },
                        edit: {
                            route: '{trustStoreId}/edit',
                            title: Uni.I18n.translate('general.Edit', 'PKJ', 'Edit'),
                            privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                            controller: 'Pkj.controller.TrustStores',
                            action: 'showEditTrustStore',
                            callback: function (route) {
                                this.getApplication().on('trustStoreLoaded', function (name) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('general.editX', 'PKJ', "Edit '{0}'"), name));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                },
                certificates: {
                    title: Uni.I18n.translate('general.certificates', 'PKJ', 'Certificates'),
                    privileges: Pkj.privileges.CertificateManagement.view,
                    route: 'certificates',
                    controller: 'Pkj.controller.Certificates',
                    action: 'showCertificates',
                    items: {
                        add: {
                            route: 'add',
                            title: Uni.I18n.translate('general.addCertificate', 'PKJ', 'Add certificate'),
                            privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                            controller: 'Pkj.controller.Certificates',
                            action: 'showAddCertificatePage'
                        },
                        addcsr: {
                            route: 'addcsr',
                            title: Uni.I18n.translate('general.addCSR', 'PKJ', 'Add CSR'),
                            privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                            controller: 'Pkj.controller.Certificates',
                            action: 'showAddCSRPage'
                        },
                        view: {
                            route: '{certificateId}',
                            title: '&nbsp;',
                            privileges: Pkj.privileges.CertificateManagement.view,
                            controller: 'Pkj.controller.Certificates',
                            action: 'showCertificateDetailsPage',
                            callback: function (route) {
                                this.getApplication().on('certificateLoaded', function (name) {
                                    route.setTitle(name);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                import: {
                                    route: 'import',
                                    title: Uni.I18n.translate('general.importCertificate', 'PKJ', 'Import certificate'),
                                    privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                                    controller: 'Pkj.controller.Certificates',
                                    action: 'showImportCertificatePage'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
