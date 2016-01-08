=======================================
Installing MOTECH Using Docker ("Beta")
=======================================

.. note::
    These instructions assume you're running on Ubuntu. If you are using MAC OSX or Windows, consider using `Docker Toolbox <https://www.docker.com/docker-toolbox>`_ for running Docker and Docker Compose. After installing and running the Docker Toolbox, the steps for running MOTECH containers shouldn't be much different.

This document provides instructions for creating a MOTECH environment using `Docker <http://www.docker.io>`_ containers. These instructions are "in beta" (the *official* installation guide is still the one :doc:`here <dev_install>`), but many members of the MOTECH dev team have been following this approach with success. This installation method is much faster than the official route.

There are two supported ways to install MOTECH with Docker:

1. As an implementer - follow this approach if you want to install a released version of MOTECH.
2. As a developer - follow this approach if you will be developing MOTECH and want to build the platform and modules from source code. If you install as developer, only the development environment will be set up for you - you will have to build and deploy MOTECH yourself.

Get Docker, Docker-Compose and motech-docker
============================================

Whether you're installing as an implementer or a developer, you'll need Docker and Docker-Compose:

Docker
------

1. Follow the instructions on the `Docker website <https://docs.docker.com/installation/>`_ to get the latest version of Docker.
2. Execute the following to configure Docker to work for non-root users: 

    .. code-block:: bash

        sudo groupadd docker
        sudo gpasswd -a ${USER} docker (logout and re-login)
        sudo service docker restart

Docker-Compose
---

Execute the following to `install Docker-Compose <https://docs.docker.com/compose/install/>`_ in Linux:

.. code-block:: bash

    sudo curl -L https://github.com/docker/compose/releases/download/1.2.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    
If you get a memory error, you may need to run these commands in root (sudo -i)

motech-docker
-------------

Clone the `motech-docker <https://github.com/motech/motech-docker>`_ project from GitHub or download it as a zip file and extract it. You'll need to run all  commands from the motech-docker directory.

.. code-block:: bash

    sudo apt-get install git
    git clone https://github.com/motech/motech-docker
    cd motech-docker


Implementer Setup
=================

Go to your motech-docker directory. To setup as an implementer (everything is automagically installed):

.. code-block:: bash

    ./setup_as_imp.sh

Type the following to start MOTECH in the background:

.. code-block:: bash

    docker-compose up -d

Voila! MOTECH has started. Wait a little bit (about 30s) then direct your browser to: http://localhost:8080/motech-platform-server

.. note::
    'docker-compose up' ERASES ALL YOUR DATA (well not really all, but pretend it does). You have to run it at least once to setup MOTECH. If you run it again, it'll erase everything you did in MOTECH. It's useful to start afresh, but remember: it nukes everything!

Developer Setup
===============

Go to your motech-docker directory. To setup as a dev:

.. code-block:: bash

    ./setup_as_dev.sh

Type the following to start all the pieces that MOTECH needs to run in the background:

.. code-block:: bash

    docker-compose up -d

Once you start the containers with the docker-compose up -d command above and *before* you build MOTECH for the first time. If you wish to add additional modules to MOTECH, then you can either use the Admin UI or copy them into /root/.motech/bundles directory of the container.

Conveniently, the container's /root/.motech/bundles directory is exposed as the docker-motech-bundles directory (with a-rw access) in your home directory (also note that the container's /root/.motech/config dir is also exposed as ~/docker-motech-config). So, you can either manually copy the binaries you require, or you can create a symbolic link to ~/docker-motech-bundles from ~/.motech/bundles.

Assuming the latter, and that you never built MOTECH before, you'd run the following commands:

.. code-block:: bash

    # go to your home dir
    cd
    # create the .motech dir
    mkdir .motech
    # create the symlink
    ln -s ~/docker-motech-bundles .motech/bundles

If you built MOTECH before, you can just delete the bundles directory and create the symlink using the command above.

Build, deploy and run MOTECH: see the :doc:`Developer Installation Guide <dev_install>`:.

.. note::

    For your convenience, the max upload in the Tomcat Manager is already increased to accept the MOTECH war.

Some Useful Docker Compose Commands
========================

Stop MOTECH
-----------

.. code-block:: bash

    docker-compose stop

Restart MOTECH
--------------

.. code-block:: bash

    docker-compose start

Watching logs
-------------

To watch all the logs (very verbose):

.. code-block:: bash

    docker-compose logs

To watch only the tomcat logs:

.. code-block:: bash

    docker-compose logs tomcat

See the sections in the generated docker-compose.yml to see what other logs you can watch.