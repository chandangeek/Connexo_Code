/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddCSR', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.csr-add',
    requires: [
        'Pkj.view.AddCSRForm'
    ],

    cancelLink: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.addCSR', 'PKJ', 'Add CSR'),
                items: {
                    xtype: 'csr-add-form',
                    cancelLink: this.cancelLink
                }
            }
        ];
        me.callParent(arguments);
    }
});