/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralDetail', {
    extend: 'Uni.view.container.ContentContainer',
    itemId: 'device-register-configuration-general-detail',
    requires: [Mdc.util.LinkPurpose],
    deviceId: null,
    registerId: null,
    linkPurpose: Mdc.util.LinkPurpose.properties[Mdc.util.LinkPurpose.NOT_APPLICABLE]
});