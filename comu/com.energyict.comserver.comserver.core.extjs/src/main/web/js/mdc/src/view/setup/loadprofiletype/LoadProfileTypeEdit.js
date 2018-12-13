/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeEditForm',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesGrid',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                    xtype: 'container',
                    layout: 'card',
                    itemId: 'load-profile-type-edit-registerTypes',
                    items: [
                        {
                            xtype: 'load-profile-type-add-register-types-grid',
                            itemId: 'load-profile-type-add-register-types-grid',
                            ui: 'large',
                            cancelHref: me.currentRoute
                        },
                        {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'no-items-found-panel',
                                    margin: '15 0 20 0',
                                    title: Uni.I18n.translate('loadprofile.edit.empty.title', 'MDC', 'No register types found'),
                                    reasons: [
                                        Uni.I18n.translate('loadprofile.edit.empty.list.item1', 'MDC', 'No register types are defined yet'),
                                        Uni.I18n.translate('loadprofile.edit.empty.list.item2', 'MDC', 'All register types are already added to the load profile type')
                                    ]
                                },
                                {
                                    xtype:'button',
                                    ui: 'link',
                                    itemId:'overview-add-reading-type-button',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    href: me.currentRoute
                                }
                            ]
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});