/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.button.Action
 */
Ext.define('Uni.button.Action', {
    extend: 'Ext.button.Button',
    alias: 'widget.uni-button-action',
    text: Uni.I18n.translate('general.actions', 'UNI', 'Actions'),
    itemId: 'actionButton',
    iconCls: 'icon icon-cog2',
    disabled: false,
    menuAlign: 'tr-br',
    listeners: {
        beforerender: function (button) {
            button.menu.items.each(function (item) {
                if ((item.visible === undefined || item.visible === true)
                    && (item.disabled === undefined || item.disabled === false)) {
                    button.enable();
                    return 0;
                }
            })
        }
    }
});