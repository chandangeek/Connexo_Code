Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('usagepoint.label.usagepoint', 'IMT', 'Usage point'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                xtype: 'menu',
                items: [
                    {
                        text: me.mRID,
                        itemId: 'usage-point-overview-link',
                        href: me.router.getRoute('usagepoints/view').buildUrl({mRID: me.mRID})
                    },
                    {
                        text: Uni.I18n.translate('general.history', 'IMT', 'History'),
                        itemId: 'usage-point-history-link',
                        href: me.router.getRoute('usagepoints/view/history').buildUrl({mRID: me.mRID})
                    },
                    {
                        text: Uni.I18n.translate('general.servicecalls', 'IMT', 'Service calls'),
                        itemId: 'usage-point-service-calls-link',
                        privileges: Scs.privileges.ServiceCall.view,
                        href: me.router.getRoute('usagepoints/view/servicecalls').buildUrl({mRID: me.mRID})
                    }
                ]
            },
            {
                text: Uni.I18n.translate('usagepoint.processes', 'IMT', 'Processes'),
                privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                itemId: 'usage-point-processes-link',
                href: me.router.getRoute('usagepoints/view/processes').buildUrl({mRID: me.mRID})
            },
            {
                title: 'Data sources',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('usagepoint.label.channel.list', 'IMT', 'Channels'),
                        itemId: 'usage-point-channels-link',
                        href: me.router.getRoute('usagepoints/view/channels').buildUrl({mRID: me.mRID})
                    },
                    {
                        text: Uni.I18n.translate('usagepoint.label.register.list', 'IMT', 'Registers'),
                        itemId: 'usage-point-registers-link',
                        href: me.router.getRoute('usagepoints/view/registers').buildUrl({mRID: me.mRID})
                    }
                ]
            },
            {
                title: 'Reading quality',
   //             xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('usagepoint.label.validation.configuration', 'IMT', 'Validation configuration'),
                        itemId: 'usage-point-val-config-link',
                    //    privileges: Cfg.privileges.Validation.fineTuneValidation,
                        href: me.router.getRoute('usagepoints/view/datavalidation').buildUrl({mRID: me.mRID})
  //                      showCondition: me.device.get('hasLogBooks') || me.device.get('hasLoadProfiles') || me.device.get('hasRegisters')
  
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
