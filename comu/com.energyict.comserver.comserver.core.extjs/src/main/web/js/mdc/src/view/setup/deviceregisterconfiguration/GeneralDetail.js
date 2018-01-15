/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralDetail', {
    extend: 'Uni.view.container.ContentContainer',
    itemId: 'device-register-configuration-general-detail',
    requires: [Mdc.util.LinkPurpose],
    deviceId: null,
    registerId: null,

    // Make sure the Mdc.Util.LinkPurpose is defined when this class is loaded
    linkPurpose: function(){
        var linkPurposeClass = Ext.ClassManager.getByAlias('LinkPurpose');
        return linkPurposeClass.properties[linkPurposeClass.NOT_APPLICABLE];
    }
});