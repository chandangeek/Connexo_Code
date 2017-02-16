/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeEditForm',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid'
    ],
    alias: 'widget.load-profile-type-edit',
    currentRoute: null,

    initComponent: function () {
        var me = this;

        me.content = {
            itemId: 'load-profile-type-edit',
            layout: 'card',
            ui: 'large',
            items: [
                {
                    xtype: 'load-profile-type-edit-form',
                    itemId: 'load-profile-type-edit-form',
                    ui: 'large'
                },
                {
                    xtype: 'load-profile-type-add-register-types-grid',
                    itemId: 'load-profile-type-add-register-types-grid',
                    ui: 'large',
                    cancelHref: me.currentRoute
                }
            ]
        };

        me.callParent(arguments);
    }
});