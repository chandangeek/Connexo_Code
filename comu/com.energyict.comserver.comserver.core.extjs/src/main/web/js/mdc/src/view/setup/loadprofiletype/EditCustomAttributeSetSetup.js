/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.EditCustomAttributeSetSetup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.loadprofiletype.EditCustomAttributeSetForm'
    ],
    alias: 'widget.edit-custom-attribute-set-setup',
    itemId: 'edit-custom-attribute-set-setup-id',

    content: [
        {
            xtype: 'edit-custom-attribute-set-form',
            ui: 'large',
            title: Uni.I18n.translate('general.loadProfileTypes.edit', 'MDC', 'Edit load profile type'),
            itemId: 'edit-load-profile-type-form-panel'
        }
    ]
});

