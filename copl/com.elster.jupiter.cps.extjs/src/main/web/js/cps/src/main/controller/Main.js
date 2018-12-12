/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.main.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.PortalItems'
    ],

    controllers: [
        'Cps.main.controller.History',
        'Cps.customattributesets.controller.AttributeSets'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Cps.main.controller.History'); // Forces route registration.

        if (Cps.privileges.CustomAttributeSets.canView()) {
            var customAttributeSetsItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.customAttributeSetsManagement', 'CPS', 'Custom attribute sets management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.customAttributeSets', 'CPS', 'Custom attribute sets'),
                        href: '#/administration/customattributesets',
                        route: 'customattributesets'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                customAttributeSetsItem
            );
        }
        me.getApplication().fireEvent('cfginitialized');
    }
});