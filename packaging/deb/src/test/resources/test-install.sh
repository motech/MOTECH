#!/bin/bash

YES="-y --force-yes"

function purge_motech() {
    $CHROOT apt-get purge motech-base $YES
    $CHROOT rm -rf /var/log/motech
    $CHROOT rm -rf /var/cache/motech
    $CHROOT rm -rf /usr/share/motech
    $CHROOT rm -rf /etc/motech
    $CHROOT rm -rf /var/lib/motech
    $CHROOT rm -f /etc/init.d/motech
}

while getopts "d:b:" opt; do
	case $opt in
	d)
        CHROOT_DIR=$OPTARG
	;;
	b)
	    BUILD_DIR=$OPTARG
	;;
    esac
done

if [ -z $CHROOT_DIR ]; then
    echo "Chroot dir not defined" >&2
    exit 1
fi

BASE_PACKAGE=`ls $BUILD_DIR | grep motech-base`

if [ ! -f $BUILD_DIR/$BASE_PACKAGE ]; then
    echo "Base package does not exist: $BASE_PACKAGE" >&2
    exit 1
fi

MAKEROOT=""
if [[ $EUID -ne 0 ]];then
    MAKEROOT="sudo"
fi

CHROOT="$MAKEROOT chroot $CHROOT_DIR"

MOTECH_OWNED="/var/lib/motech /var/cache/motech /usr/share/motech/.motech"
NON_MOTECH_OWNED="/var/lib/motech /var/cache/motech /usr/share/motech/.motech"

# Remove previous installation if any
purge_motech

# Install package
cp $BUILD_DIR/$BASE_PACKAGE $CHROOT_DIR/tmp
$CHROOT dpkg -i /tmp/$BASE_PACKAGE
$CHROOT apt-get install -f # install dependencies
$CHROOT service motech start

# Make sure files/directories exist with correct permissions

for dir in $MOTECH_OWNED; do
    if [ `$CHROOT stat -c %U /var/lib/motech` != "motech" ]; then
        echo "$dir is not owned by motech" >&2
        purge_motech
        exit 1
    fi
done

for dir in $NON_MOTECH_OWNED; do
    $CHROOT file $dir # returns 1 if failed
    RET=$?
    if [ $RET -ne 0 ]; then
        echo "$dir does not exist" >&2
        purge_motech
        exit $RET
    fi
done

# Give motech some time
sleep 5

# Check the homepage
curl -L localhost:8080 | grep -i motech
RET=$? # Success?
if [ $RET -ne 0 ]; then
    echo "Failed getting motech page" >&2
    purge_motech
    exit $RET
fi

$CHROOT service motech stop

# Make sure some dirs are empty, so they can be removed
$CHROOT rm -rf /var/log/motech/*

# Remove motech
$CHROOT apt-get remove motech-base $YES

for dir in $MOTECH_OWNED" "$NON_MOTECH_OWNED; do
    $CHROOT file $dir # will return 0 if exists
    RET=$?
    if [ $RET -eq 0 ]; then
        echo "$dir still exists after uninstall" >&2
        purge_motech
        exit 1
    fi
done

exit 0 # Victory