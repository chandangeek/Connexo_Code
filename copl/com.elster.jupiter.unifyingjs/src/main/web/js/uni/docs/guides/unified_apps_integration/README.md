# Unified apps integration

## Introduction

All created extjs applications should be able to run from one 'master' application. Depending on the different deployed bundles. The master application should show more or less menu items.
For example if the masterjs bundle and the Mdv bundle is deployed, the master application only has one menuitem. If the masterjs bundle, the MDC bundle and the validation bundle is deployed the master application will have 2 menuitems, one for Mdc and 1 for validation.

In order for this to work, the masterjs bundle has been created (com.elster.jupiter.masterjs)

For now on all development should be done suing the master application, because some extjs functionality that works in stand-alone applications will NOT work when running this application from the master application (more on this later)

below is an explanation on how to integrate your application in the master application. If stuck, a complete working bundle that runs from the master application can be found at:

com.energyict.comserver.core.extjs

## How to integrate an existing application

An existing stand-alone application can be integrated in the master application by simply changing it 'Activator' class. (see Activator changes). After this you application will appear in the master application on start-up.

However:

The application will probalbe break, because certain 'magic' functions of extjs are not usable anymore.
These functions can not work anymore, because the application context has changed. your application will no longer have its own context, only the contect of the master application will exist at runtime.

Things that will need to change in you application:

1. No more short notation of controllers (or anything for that matter)

example

use:
     controllers: [
            'Mdc.controller.setup.ComPortPools',
            'Mdc.controller.setup.DeviceTypes',
            'Mdc.controller.setup.SetupOverview'
     ]

in stead of:

     controllers: [
            'ComPortPools',
            'DeviceTypes',
            'SetupOverview'
     ]

2. No more 'magic' methods:

Extjs creates magic methods for your controllers. These will not work anymore because they start searching for your controller from the application context, which has changed.

example

use:

    this.getController('Mdc.controller.setup.ComPortPools');

in stead of:

    this.getApplication().getSetupComPortPoolsController();


### Activator changes

The activator will have to be changed in the following way:

        public void start(BundleContext bundleContext) throws Exception {
            String alias = "/mdc";
            //create a list of scripts with their path, if your application uses third party js files
            List<Script> scripts = new ArrayList<>();
            scripts.add(new Script("jquery","/path/to/jquery"));
            // create a new defaultstartpage with the as it used to be, but now add also the main controller of your application,
            // the list of third party scripts and a list of translation components
            DefaultStartPage mdc = new DefaultStartPage("Mdc", "", "/index.html", "Mdc.controller.Main",scripts,Arrays.asList("MDC"));
            HttpResource resource = new HttpResource(alias, "/js/mdc" , new BundleResolver(bundleContext), mdc);
            registration = bundleContext.registerService(HttpResource.class, resource , null);
        }

        This activator will register your bundle with the whiteboard. When the master application is started, the master frontend will query the backend for available applications and dynamically load the main controller of your application,
        the scripts and the translation components. When this is done your application should appear in the menu on the master application.

### Main controller changes

The master application will call your 'main' controller. This means that app.js and application.js will not be used when starting the application from within the master.
So all functionality relevant to your application (but not loading functionality) that resides in these files should be moved to your 'main' controller.

## Considerations

* Since each application is loaded via its activator, there is no set order. Make sure the index values of menu
  items are set correctly to avoid unexpected results.