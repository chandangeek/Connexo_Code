Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEmptyList', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypeEmptyList',
    height: 395,
    actionHref: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'panel',
                html: "<h3>No load profile types found</h3><br>\
          There are no load profile types. This could be because:<br>\
          &nbsp;&nbsp; - No load profile types have been defined yet.<br>\
          &nbsp;&nbsp; - No load profile types comply to the filter.<br><br>\
          Possible steps:<br><br>"
            },
            {
                xtype: 'button',
                text: 'Add load profile type',
                action: 'addloadprofiletypeaction',
                hrefTarget: '',
                href: this.actionHref
            }

        )
    }
});