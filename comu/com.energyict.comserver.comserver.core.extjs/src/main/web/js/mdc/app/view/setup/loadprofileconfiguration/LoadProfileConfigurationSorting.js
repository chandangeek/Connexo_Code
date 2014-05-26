Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSorting', {
    extend: 'Skyline.panel.FilterToolbar',
    alias: 'widget.loadProfileConfigurationSorting',
    title: 'Sort',
    name: 'sortitemspanel',
    height: 40,
    emptyText: 'None',
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                name: 'addsortitemmenu'
            }
        }
    ]
});