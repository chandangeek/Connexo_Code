/**
 * todo: move out!
 */
Ext.define('Uni.component.filter.view.FilterTopPanel', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.filter-top-panel',
    title: 'Filters',
    setFilter: function (key, name, value, hideIcon) {
        var me = this,
            btnsContainer = me.getContainer(),
            btn = btnsContainer.down('button[name=' + key + ']');
        if (!_.isEmpty(btn)) {
            btn.setText(name + ': ' + value);
        } else if (!hideIcon) {
            btnsContainer.add(Ext.create('Uni.view.button.TagButton', {
                text: name + ': ' + value,
                name: key,
                listeners: {
                    closeclick: function () {
                        me.fireEvent('removeFilter', key);
                    }
                }
            }));
        } else {
            btnsContainer.add(Ext.create('Ext.button.Button', {
                text: name + ': ' + value,
                name: key,
                ui: 'tag'
            }));
        }
        this.updateContainer(this.getContainer());
    }
});