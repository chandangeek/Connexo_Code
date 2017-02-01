Ext.define('Imt.rulesets.view.MetrologyConfigurationPurposesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-configuration-purposes-grid',
    requires: [
        'Uni.grid.column.RemoveAction'
    ],
    router: null,
    addLink: null,
    adminPrivileges: null,

    initComponent: function () {
        var me = this,
            route = me.router.getRoute('administration/metrologyconfiguration/view');

        me.columns = [
            {
                header: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                dataIndex: 'metrologyConfigurationInfo',
                flex: 1,
                renderer: function (value) {
                    if (Imt.privileges.MetrologyConfig.canView() || Imt.privileges.MetrologyConfig.canAdministrate()) {
                        return Ext.String.format('<a href="{0}">{1}</a>', route.buildUrl({mcid: value.id}), value.name);
                    } else {
                        return value.name;
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.metrologyConfigurationStatus', 'IMT', 'Metrology configuration status'),
                dataIndex: 'isActive',
                flex: 1,
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.active', 'IMT', 'Active')
                        : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                }
            },
            {
                header: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                dataIndex: 'metrologyContractInfo',
                renderer: function (value) {
                    return value.name
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn-remove',
                privileges: me.adminPrivileges
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} metrology configuration purposes'),
                displayMoreMsg: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} metrology configuration purposes'),
                emptyMsg: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.pagingtoolbartop.emptyMsg', 'IMT', 'There are no metrology configuration purposes to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                        href: me.addLink,
                        action: 'addMetrologyConfigurationPurposes',
                        itemId: 'grid-add-metrology-configuration-purposes-button',
                        privileges: me.adminPrivileges
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Metrology configuration purposes per page')
            }
        ];

        me.callParent(arguments);
    }
});