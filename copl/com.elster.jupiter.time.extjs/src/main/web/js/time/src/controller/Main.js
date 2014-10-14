Ext.define('Tme.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems'
    ],
    controllers: [
        'Tme.controller.RelativePeriods'
    ],
    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this;
        var portalItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.portalitem', 'TME', 'Data Export'),
            portal: 'dataexport',
            route: 'dataexport',
            items: [
                {
                    text: Uni.I18n.translate('general.relativeperiods', 'TME', 'Relative Periods'),
                    href: '#/dataexport/relativeperiods',
                    route: 'relativeperiods'
                }
            ]
        });

        Uni.store.PortalItems.add(
            portalItem
        );
        this.getApplication().fireEvent('cfginitialized');
    }

});

