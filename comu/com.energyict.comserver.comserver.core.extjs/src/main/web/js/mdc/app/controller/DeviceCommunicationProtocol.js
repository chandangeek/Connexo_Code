Ext.define('Mdc.controller.DeviceCommunicationProtocol', {
    extend: 'Ext.app.Controller',

    stores: [
        'DeviceCommunicationProtocols'
    ],

    models: [
        'DeviceCommunicationProtocol'
    ],

    views: [
        'devicecommunicationprotocol.Browse',
        'devicecommunicationprotocol.Edit'
    ],

    init: function () {
        //this.initMenu();

        this.control({
            '#deviceCommunicationProtocolList': {
                itemdblclick: this.editDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=save]': {
                click: this.updateDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolEdit button[action=clone]': {
                click: this.cloneDeviceCommunicationProtocol
            },
            'deviceCommunicationProtocolList button[action=save]': {
                click: this.saveDeviceCommunicationProtocol
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Protocols',
            href: Mdc.getApplication().getHistoryDeviceCommunicationProtocolController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('deviceCommunicationProtocolBrowse');
        Mdc.getApplication().getMainController().showContent(widget);
    },

    editDeviceCommunicationProtocol: function (grid, record) {
        var view = Ext.widget('deviceCommunicationProtocolEdit');
        view.down('form').loadRecord(record);
        this.resetGroupsForRecord(record);
    },
    updateDeviceCommunicationProtocol: function (button) {
        var win = button.up('window') ,
            form = win.down('form'),
            record = form.getRecord(),
            values = form.getValues()

        record.set(values);

        win.close();
        record.save();
        record.commit();
    },
    cloneDeviceCommunicationProtocol: function (button) {
        var me = this,
            win = button.up('window') ,
            form = win.down('form'),
            record = form.getRecord().copy(),
            values = form.getValues();

        record.set(values);
        win.close();
        record.setId(null);
        record.phantom = true;

        record.save({
            callback: function () {
                record.commit();
                me.getDeviceCommunicationProtocolsStore().add(record);
            }
        });
    },
    saveSuccess: function () {
//        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },
    saveDeviceCommunicationProtocol: function (button) {
        this.getDeviceCommunicationProtocolStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    }
});