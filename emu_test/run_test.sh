#!/bin/bash
# It is to be used with BYOB setup to run CTS tests.
#
# It takes 1 command line argument.
# DIST_DIR => Absolute path for the distribution directory.
#
# It will return 0 if it is able to execute tests, otherwise
# it will return 1.
#
# Owner: akagrawal@google.com

DIST_DIR=$1

BUILD_DIR="out/prebuilt_cached/builds"

if [ ! -d "$BUILD_DIR/test_suite" ];
then
    echo "CTS suite does not exist"
    exit 1
fi
TEST_SUITE=`ls $BUILD_DIR/test_suite`
echo "$TEST_SUITE"

EMU_BIN=5430293
echo "$EMU_BIN"

if [ -d "$BUILD_DIR/gphone_x86-user" ];
then
    SYS_IMAGE=`ls $BUILD_DIR/gphone_x86-user`
    echo "Run CTS with $SYS_IMAGE"
fi

if [ -d "$BUILD_DIR/gphone_x86-user" ];
then
    SYS_IMAGE_64=`ls $BUILD_DIR/gphone_x86_64-user`
    echo "Run CTS with $SYS_IMAGE_64"
fi

exit 0
