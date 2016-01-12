# URL Routing with Crossroads.js

## Introduction

In order to have more clear routing possibilities in our application, it was decided to use the crossroads.js library.
This library makes it possible to add routes to your application in a concise and readable way.

[crossroads website](http://millermedeiros.github.io/crossroads.js/)

## Requirements

When using your application from the "masterjs" application (as you should). There is almost no setup. The library is loaded when starting the master application.
The only thing needed is setting up your routes in your setup (see below)

When using a stand-alone application you will have to load the libraries in your index.html

## How to use

Setup your routes in your history setup, and make crossroads parse the path. Simple as that.
The converter.js in the unifyingjs bundle has been changed, so that it also send the complete path to your doConversion method (before it was only an array of tokens) so that the doCoversion method can delegate the parsing to crossroads.
example:

        Ext.define('Mdc.controller.history.Setup', {
            extend: 'Uni.controller.history.Converter',

            rootToken: 'setup',
            previousPath: '',
            currentPath: null,

            init: function () {
                var me = this;
                crossroads.addRoute('setup',function(){
                    me.getApplication().getController('Mdc.controller.setup.SetupOverview').showOverview();
                });

                //Device type routes
                crossroads.addRoute('setup/devicetypes',function(){
                    me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceTypes();
                });
                crossroads.addRoute('setup/devicetypes/create',function(){
                    me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeCreateView(null);
                });
                crossroads.addRoute('setup/devicetypes/{id}',function(id){
                    me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeDetailsView(id);
                });
                crossroads.addRoute('setup/devicetypes/{id}/edit',function(id){
                    me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeEditView(id);
                });

                this.callParent(arguments);
            },

            doConversion: function (tokens,token) {
                //now has tokens and token (which is you complete path)

                var queryStringIndex = token.indexOf('?');
                if (queryStringIndex > 0) {
                    token = token.substring(0, queryStringIndex);
                }
                if (this.currentPath !== null) {
                    this.previousPath = this.currentPath;
                }
                this.currentPath = token;
                crossroads.parse(token);
            },

            ...

For a full exemple check out 'Setup.js' in the 'com.energyict.comserver.core.extjs' bundle

### Simple routes

        crossroads.addRoute('setup/devicetypes',function(){
              me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceTypes();
        });

### Parameterized routes

        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/edit',function(deviceTypeId,deviceConfigurationId){
              me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationEditView(deviceTypeId,deviceConfigurationId);
        });)

### Advanced routing

See extended documentation at: [crossroads website](http://millermedeiros.github.io/crossroads.js/)

## Notes

* Be mindful of route order if the [route is greedy](http://millermedeiros.github.io/crossroads.js/#route-greedy).

## Links and resources

* http://millermedeiros.github.io/crossroads.js/