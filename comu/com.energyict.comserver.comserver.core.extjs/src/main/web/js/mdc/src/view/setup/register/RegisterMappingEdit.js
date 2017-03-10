/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.register.RegisterMappingEditForm'
    ],
    alias: 'widget.register-mapping-edit-container',
    itemId: 'register-mapping-edit-container-id',

    content: [
        {
            xtype: 'edit-register-mapping-form',
            ui: 'large',
            title: Uni.I18n.translate('general.registerType.edit','MDC','Edit register type'),
            itemId: 'edit-register-type-form-panel'
        }
    ]
});

