/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.systemprops.SysPropsContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.system-props-container',
    requires: [
        'Sam.view.systemprops.SystemPropsView'
    ],

    router: null,

    viewDefaults: {
        xtype: 'displayfield',
        labelWidth: 150
    },
    editDefaults: {
        labelWidth: 150,
        anchor: '100%',
        maxWidth: 421
    },


    refs: [
            {
                ref: 'cont',
                selector: 'sys-prop-content'
            }
    ],

    initComponent: function () {
        var me = this;
        me.canEditSysProps = Uni.Auth.checkPrivileges(Sam.privileges.SystemProperties.admin);

        me.content = [
            {
                itemId: 'sys-prop-content',
                title: Uni.I18n.translate('general.systemProperties', 'SAM', 'System properties'),
                ui: 'large',
                layout: 'hbox',
                defaults: {
                    flex: 1
                },
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'sys-prop-attributes-actions-button',
                        privileges: me.canEditSysProps,
                        margin: '0 16 0 0',
                        menu: {
                            xtype: 'menu',
                            itemId: 'sys-prop-attributes-actions-menu',
                            plain: true,
                            items: [
                                {
                                    text: Uni.I18n.translate('general.editSystemProperties', 'SAM', 'Edit'),
                                    itemId: 'edit-system-properties'
                                }
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        defaults: {
                            ui: 'tile2'
                        },
                        items: [
                            {
                                xtype: 'system-props-view',
                                itemId: 'sys-props-attributes-form',
                                title: Uni.I18n.translate('general.cacheParameters', 'SAM', 'Cache parameters'),
                                router: me.router,
                                viewDefaults: me.viewDefaults,
                                editDefaults: me.editDefaults
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },


    loadRecord: function (record) {
            var me = this;
            me.down('#sys-props-attributes-form').loadRecord(record);
    }
});