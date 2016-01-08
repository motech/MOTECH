============
Introduction
============
Motech provides `Maven archetypes <http://maven.apache.org/guides/introduction/introduction-to-archetypes.html>`__ in its `Nexus repository <http://nexus.motechproject.org/content/repositories/releases/>`__ which you can use to create a new Motech module.  The archetypes supply basic source code needed for a new module, as well as configuration files for packaging the module as a bundle to be loaded into a Motech server.

The first archetype is the *minimal bundle archetype*.  This supplies just enough source code and configuration to make a "Hello World" module.

Additional archetypes can add functionality to the minimal archetype:
 * The *http bundle archetype* adds a servlet to respond to HTTP requests, and a simple web user interface.
 * The *repository bundle archetype* adds a repository layer for storing and retrieving data from :doc:`MOTECH's data services <model_data>`.
 * The *settings bundle archetype* adds a properties file to store custom module configuration, and exposes the configuration through Motech's web user interface

Any combination of these additional archetypes may be added to the minimal archetype.

########################
Minimal Bundle Archetype
########################
To create a new minimal bundle from the minimal bundle archetype, use the following command::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

This will create a new Maven project in your current directory.

This is a long command.  Here is an explanation of the parameters:

+----------------------+---------------------------------------------------------------+-------------------------------------------+
| *parameter*          |  *value*                                                      | *explanation*                             |
+======================+===============================================================+===========================================+
| -DinteractiveMode    |  false                                                        | no need to wait for user input            |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DarchetypeRepository|  http://nexus.motechproject.org/content/repositories/releases | where to find the archetype               |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DarchetypeGroupId   |  org.motechproject                                            | group name for the archetype              |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DarchetypeArtifactId|  minimal-bundle-archetype                                     | which archetype to use                    |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DmotechVersion      |  0.26-SNAPSHOT                                                | Motech version to use with the new module |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DgroupId            |  org.motechproject                                            | group name for the new module             |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DartifactId         |  motech-test-module                                           | artifact name for the new module          |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -Dpackage            |  archetype.test                                               | Java package for new module classes       |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -Dversion            |  0.1-SNAPSHOT                                                 | version of the new module itself          |
+----------------------+---------------------------------------------------------------+-------------------------------------------+
| -DbundleName         |  "Archetype Test Module"                                      | name of the new module                    |
+----------------------+---------------------------------------------------------------+-------------------------------------------+

#####################
HTTP Bundle Archetype
#####################
To create a new bundle that has HTTP support, use the following two commands from the same directory.

Create a minimal bundle with configuration modified for HTTP::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module" -Dhttp=true

Note the new parameter:

+--------+----+
|-Dhttp  |true|
+--------+----+

Add new source files from the HTTP archetype::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=http-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

Note the new archetype Id:

+---------------------+---------------------+
|-DarchetypeArtifactId|http-bundle-archetype|
+---------------------+---------------------+

.. attention::

    For the names of the new controllers in angular you should add prefix associated with the module so that the name is unique.

###########################
Repository Bundle Archetype
###########################
To create a new bundle that has support for :doc:`MOTECH's data services <model_data>`, use the following two commands from the same directory.

Create a minimal bundle with configuration modified for repository::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module" -Drepository=true

Add new source files from the repository archetype::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=repository-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

#########################
Settings Bundle Archetype
#########################
To create a new bundle that has module settings support, use the following two commands from the same directory.

Create a minimal bundle with configuration modified for settings::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module" -Dsettings=true

Add new source files from the settings archetype::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=settings-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

#########################
Combined Bundle Archetype
#########################
The minimal bundle archetype can be supplemented with any combination of additional archetypes.  To create a bundle that uses them all, use all the following commands from the same directory.

Create a minimal bundle with configuration modified for all additional archetypes::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module" -Dhttp=true -Drepository=true -Dsettings=true

Add source files from all the additional archetypes::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=http-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=repository-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

    mvn archetype:generate -DinteractiveMode=false -DarchetypeRepository=http://nexus.motechproject.org/content/repositories/releases -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=settings-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

########################
Using Archetypes Locally
########################
You can also use the archetypes locally, without the Motech Nexus repository.  First, you must build the archetypes locally.  You can either follow the :doc:`developer guidelines </development/dev_procedures/patch>` to set up your developer environemt, or to build locally without commiting::

    git clone https://github.com/motech/motech/
    cd motech
    mvn clean install

Then you can use the archetypes from your Maven local catalog::

    mvn archetype:generate -DinteractiveMode=false -DarchetypeCatalog=local -DarchetypeGroupId=org.motechproject -DarchetypeArtifactId=minimal-bundle-archetype -DmotechVersion=0.26-SNAPSHOT -DgroupId=org.motechproject -DartifactId=motech-test-module -Dpackage=archetype.test -Dversion=0.1-SNAPSHOT -DbundleName="Archetype Test Module"

Note the new parameter:

+------------------+-----+
|-DarchetypeCatalog|local|
+------------------+-----+
