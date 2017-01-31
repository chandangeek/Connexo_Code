/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.Auth',
        'Cfg.privileges.Validation',
        'Ddv.controller.Validations'
    ],

    controllers: [
        'Ddv.controller.history.Workspace',
        'Ddv.controller.Validations'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        this.initMenu();
        this.callParent();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataCollection = null,
            items = [],
            historian = me.getController('Ddv.controller.history.Workspace'); // Forces route registration.

        if (Cfg.privileges.Validation.canView()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'DDV', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

            items.push({
                text: Uni.I18n.translate('validation.validations.title', 'DDV', 'Validations'),
                href: '#/workspace/validations'
            });

            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataValidation', 'DDV', 'Data validation'),
                portal: 'workspace',
                route: 'validations',
                items: items
            });

            Uni.store.PortalItems.add(dataCollection);
        }
    },
    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
