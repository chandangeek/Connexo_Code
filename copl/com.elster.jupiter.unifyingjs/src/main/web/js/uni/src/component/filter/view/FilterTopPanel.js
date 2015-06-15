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
            str = '',
            btn = btnsContainer.down('button[name=' + key + ']');
        if (!_.isEmpty(btn)) {
            btn.setText(name + ': ' + value);
        } else if (!hideIcon) {
            if (Ext.isArray(value)) {
                Ext.Array.each(value, function (item) {
                    if (item !== value[value.length-1]) {
                        str += item + ', ';
                    } else {
                        str += item;
                    }
                });
            } else {
                str = value;
            }
            btnsContainer.add(Ext.create('Uni.view.button.TagButton', {
                text: name + ': ' + Ext.String.htmlEncode(str),
                name: key,
                listeners: {
                    closeclick: function () {
                        me.fireEvent('removeFilter', key);
                    }
                }
            }));
        } else {
            btnsContainer.add(Ext.create('Ext.button.Button', {
                text: name + ': ' + Ext.String.htmlEncode(value),
                name: key,
                ui: 'tag'
            }));
        }
        this.updateContainer(this.getContainer());
    }
});