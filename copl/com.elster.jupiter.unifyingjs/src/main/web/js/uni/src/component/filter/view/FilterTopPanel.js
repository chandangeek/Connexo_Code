Ext.define('Uni.component.filter.view.FilterTopPanel', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.filter-top-panel',
    title: 'Filters',
    setFilter: function (key, name, value) {
        var me = this,
            btnsContainer = me.getContainer(),
            btn = btnsContainer.down('button[name=' + key + ']');
        if (!_.isEmpty(btn)) {
            btn.setText(name + ': ' + value);
        } else {
            btnsContainer.add(Ext.create('Uni.view.button.TagButton', {
                text: name + ': ' + value,
                name: key,
                listeners: {
                    closeclick: function (btn) {
                        me.fireEvent('removeFilter', key);
                    }
                }
            }));
        }
        me.updateContainer(btnsContainer);
    }
});