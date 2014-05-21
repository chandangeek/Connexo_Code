Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSorting', {
    extend: 'Skyline.panel.FilterToolbar',
    alias: 'widget.securitySettingSorting',
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