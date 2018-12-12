/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.privileges.CertificateManagement', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.certificates', 'privilege.administrate.certificates', 'privilege.administrate.trust.store'],
    adminTrustStores: ['privilege.administrate.trust.store'],
    adminCertificates: ['privilege.administrate.certificates'],

    all: function () {
        return Ext.Array.merge(
            Pkj.privileges.CertificateManagement.view,
            Pkj.privileges.CertificateManagement.adminTrustStores,
            Pkj.privileges.CertificateManagement.adminCertificates
        );
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Pkj.privileges.CertificateManagement.view);
    },

    canAdministrateTrustStores: function() {
        return Uni.Auth.checkPrivileges(Pkj.privileges.CertificateManagement.adminTrustStores);
    },

    canAdministrateCertificates: function() {
        return Uni.Auth.checkPrivileges(Pkj.privileges.CertificateManagement.adminCertificates);
    }

});