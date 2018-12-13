/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.Landing', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-landing-page',
    requires: [
        'Scs.view.PreviewForm',
        'Scs.view.ActionMenu',
        'Scs.view.log.Setup'
    ],
    serviceCallId: null,
    router: null,
    record: null,
    serviceCallLoaded: false,

    initComponent: function () {
        var me = this;

        me.content = {
            title: me.serviceCallId,
            xtype: 'panel',
            ui: 'large',
            itemId: 'usagePointSetupPanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tools: [
                {
                    xtype: 'uni-button-action',
                    privileges: Scs.privileges.ServiceCall.admin,
                    disabled: !me.record.get('canCancel'),
                    itemId: 'scsActionButton',
                    margin: '0 20 0 0',
                    menu: {
                        xtype: 'scs-action-menu',
                        record: me.record
                    }
                }
            ],
            items: [
                {
                    xtype: 'servicecalls-preview-form',
                    itemId: 'service-call-landing-page-form',
                    router: me.router,
                    detailed: true
                },
                {
                    xtype: 'scs-log-setup',
                    itemId: 'serviceCallLog'
                }


            ]
        };
        this.callParent(arguments);
        me.updateLandingPage(me.record);
    },

    updateLandingPage: function (record, forced) {
        var me = this,
            previewForm = me.down('#service-call-landing-page-form');
        if (Ext.isDefined(forced)) {
            me.serviceCallLoaded = false;
        }
        if (me.serviceCallLoaded === false && !Ext.isEmpty(record)) {
            previewForm.updatePreview(record);
            me.serviceCallLoaded = true; // avoid reloading when switching tabs
        }
    }
});