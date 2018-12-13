/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-edit',
    router: null,
    route: null,
    requires: ['Dlc.devicelifecycles.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'device-life-cycles-add-form',
                router: me.router,
                route: me.route,
                btnAction: 'edit',
                btnText: Uni.I18n.translate('general.save', 'DLC', 'Save'),
                hideInfoMsg: true
            }
        ];
        me.callParent(arguments);
    }
});
