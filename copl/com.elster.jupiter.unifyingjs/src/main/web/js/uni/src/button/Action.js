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
    menuAlign: 'tr-br'
});