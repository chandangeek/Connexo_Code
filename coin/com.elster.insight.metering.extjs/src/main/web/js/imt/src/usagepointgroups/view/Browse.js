/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-usagepointgroup-browse',
    xtype: 'add-usagepointgroup-browse',
    requires: [
        'Imt.usagepointgroups.view.Navigation',
        'Imt.usagepointgroups.view.Wizard'
    ],
    router: null,
    returnLink: null,
    isEdit: false,
    service: null,

    initComponent: function () {
        var me = this;

        me.side = {
            itemId: 'usagepointgroup-add-panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {                    
                    xtype: 'usagepointgroup-add-navigation',
                    itemId: 'usagepointgroup-add-navigation',
                    isEdit: me.isEdit,
                    title: me.isEdit ? ' ' : Uni.I18n.translate('usagepointgroup.wizardMenu', 'IMT', 'Add usage point group')
                }
            ]
        };

        me.content = [
            {
                xtype: 'addusagepointgroup-wizard',
                itemId: 'addusagepointgroup-wizard',
                isEdit: me.isEdit,
                router: me.router,
                service: me.service,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});
