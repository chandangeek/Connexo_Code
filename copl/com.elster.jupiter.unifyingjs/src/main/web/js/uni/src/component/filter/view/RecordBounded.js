Ext.define('Uni.component.filter.view.RecordBounded', {
    loadRecord: function (record) {
        var me = this,
            paramsCount = 0,
            data = record.getData();
        for (key in data) {
            if (data[key] && (data[key][0].length > 0)) {
                me.addFilterBtn(key, key, data[key]);
                ++paramsCount;
            }
        }
        paramsCount < 1 ? me.hide() : me.show();
        me.record = record;
    },

    addFilterBtn: function (name, propName, value) {
        var me = this,
            filterBar = me.down('filter-toolbar').getContainer(),
            btn = filterBar.down('button[name=' + name + ']');
        if (btn) {
            btn.setText(propName + ': ' + value)
        } else {
            btn = Ext.create('Skyline.button.TagButton', {
                text: propName + ': ' + value,
                name: name,
                value: value
            });
            btn.on('closeclick', function (btn) {
                me.record.set(btn.name, '');
                me.record.save()
            });
            filterBar.add(btn)
        }
    }
});
