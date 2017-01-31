/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Tme.privileges.Period',
        'Uni.store.MenuItems',
        'Uni.store.PortalItems',
        'Tme.controller.history.Time'
    ],

    controllers: [
        'Tme.controller.RelativePeriods',
        'Tme.controller.history.Time'
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Tme.controller.history.Time');
    },

    initMenu: function () {
	    if (Tme.privileges.Period.canView()) {
            var relativePeriodItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.relativePeriod', 'TME', 'Relative period'),
                    portal: 'administration',
                    items: [
                        {
                            text: Uni.I18n.translate('general.relativePeriods', 'TME', 'Relative periods'),
                            href: '#/administration/relativeperiods',
                            route: 'relativeperiods'
                        }
                    ]
                });

            Uni.store.PortalItems.add(
                relativePeriodItem
            );
		}
    }
});

