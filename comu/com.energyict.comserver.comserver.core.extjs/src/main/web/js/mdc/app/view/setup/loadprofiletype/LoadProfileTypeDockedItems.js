Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileTypeDockedItems',
    aling: 'left',
    actionHref: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'loadProfileTypesCountContainer',
                flex: 1
            },
            {
                xtype: 'button',
                text: 'Add load profile type',
                action: 'addloadprofiletypeaction',
                margin: '0 5',
                hrefTarget: '',
                href: this.actionHref
            }
        )
    }
});