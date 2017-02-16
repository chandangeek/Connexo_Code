/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeSetup',
    itemId: 'loadProfileTypeSetup',

    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicetype.SideMenu'
    ],

    showCustomAttributeSet: false,

    initComponent: function () {
        var me = this,
            config = me.config,
            addButtons;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'loadProfileTypeGrid',
                            store: config.gridStore
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'load-profile-type-empty-component',
                            title: Uni.I18n.translate('loadProfileTypes.empty.title', 'MDC', 'No load profile types found'),
                            reasons: [
                                Uni.I18n.translate('loadProfileTypes.empty.list.item1', 'MDC', 'No load profile types have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('loadProfileTypes.add.loadprofileTypes', 'MDC', 'Add load profile types'),
                                    action: 'addloadprofiletypeaction',
                                    privileges: Mdc.privileges.MasterData.admin,
                                    href: '#/administration/loadprofiletypes/add'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'loadProfileTypePreview'
                        }
                    }
                ]
            }
        ];

        if (config && config.deviceTypeId) {
            me.side = [
                {
                    xtype: 'panel',
                    ui: 'medium',
                    items: [
                        {
                            xtype: 'deviceTypeSideMenu',
                            deviceTypeId: config.deviceTypeId,
                            toggle: 2
                        }
                    ]
                }
            ];
        }

        me.callParent(arguments);

        addButtons = me.query('button[action=addloadprofiletypeaction]');
        me.down('#custom-attribute-set-displayfield-id').setVisible(me.showCustomAttributeSet);
        if (config) {

            hasPrivilege = Mdc.privileges.DeviceType.canAdministrate();
            actionMenuColumn = me.down('#load-profile-type-action-menu-column');
            actionMenuButton = me.down('#loadProfileTypePreview').tools[0];

            if (config.deviceTypeId) {
                Ext.Array.each(addButtons, function (button) {
                    button.href = '#/administration/devicetypes/' + config.deviceTypeId + '/loadprofiles/add';
                });
            }
            var emptyComponent;
            Ext.Array.each(addButtons, function (button) {
                emptyComponent = button.up('#load-profile-type-empty-component');
                if (typeof emptyComponent !== 'undefined') {
                    emptyComponent.down('#no-items-found-panel-steps-label').hidden = !hasPrivilege;
                }
                button.hidden = !hasPrivilege;

            });
            actionMenuColumn.hidden = !hasPrivilege;
            actionMenuButton.hidden = !hasPrivilege;
        }
    }
});