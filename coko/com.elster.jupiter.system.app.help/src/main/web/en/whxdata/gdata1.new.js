(function () {
    var glossary = {
        "type": "data",
        "entrys": [{
            "type": "entry",
            "name": "application server",
            "value": "Is a server on which different processes can run. With these servers the load can be balanced by dividing different tasks over different application servers. On the application server message services can be added and the number of threads can be changed to define the amount of parallel services."
        }, {
            "type": "entry",
            "name": "ghost",
            "value": "Ghost is the status of time of use calendars that originate from the device and that are not known in the Connexo system. For these time of use calendars no detailed information is available and it is not possible to preview such a time of use calendar. It can only be removed. Ghost time of use calendars can be introduced into Connexo by importing them. See “Import time of use calendar”. Import file must be requested from the device manufacturer."
        }, {
            "type": "entry",
            "name": "import service",
            "value": "Is used to import data into Connexo. The import service will take the file that needs to be imported, move it to the correct folder and subsequently put it on the queue. The actual import is handled by a message service. This means that for import to be successful import service as well as message service need to be configured and added to an application server"
        }, {"type": "entry", "name": "message services", "value": "Are the services that run on the application server for which queues are used."}, {
            "type": "entry",
            "name": "privilege",
            "value": "Designate what rights a user has to perform certain actions on a specific resource. This can be for example the viewing, adding, editing or removing of a communication server. A resource is a component of an application, e.g. role is a resource as well as issue rules, licenses and communication servers."
        }, {
            "type": "entry",
            "name": "reading type",
            "value": "A reading type provides a detailed description of a reading value. It is described using 18 key attributes separated by a dot."
        }, {
            "type": "entry",
            "name": "relative period",
            "value": "A relative period is calculated based on a specific reference date, therefore this period is relative to this reference point."
        }, {
            "type": "entry",
            "name": "resource",
            "value": "A resource is a feature or a combination of features within the application, e.g. the resource Communication infrastructure designates all features to manage the communication infrastructure such as the communication servers and ports."
        }, {
            "type": "entry",
            "name": "user directory",
            "value": "Is a place where you store information about users. User information includes the person's full name, user name, password, email address and other personal information."
        }, {
            "type": "entry",
            "name": "user role",
            "value": "User roles determine which users have access to which application and what and what not they can do in this application. User roles are used to group privileges into sets that can be assigned to different users. Every user assigned with a specific user role has all of the privileges included in that role. When a user is assigned multiple roles, then the user will have all of the privileges of each role."
        }]
    };
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), glossary, {sync: true});
})();