Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileConfigurationEmptyList', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationEmptyList',
    actionHref: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'panel',
                html: "<h3>No load profile configurations found</h3><br>\
          There are no load profile configurations. This could be because:<br>\
          &nbsp;&nbsp; - No load profile configurations have been defined yet.<br>\
          &nbsp;&nbsp; - No load profile configurations comply to the filter.<br><br>\
          Possible steps:<br><br>"
            },
            {
                xtype: 'button',
                text: 'Add load profile configuration',
                action: 'addloadprofileconfigurationaction',
                hrefTarget: '',
                href: this.actionHref
            }

        )
    }
});