#!/bin/bash
set -e

# This script is the complete cookbook for how to run all tests pertinent to the SystemUI team.
# In practice, one would rarely want to run every single test in one go, but it should be
# hypothetically possible, and any tests that fail here should be tracked and fixed.

# Pre-reqs: envsetup.sh and lunch, run from top-level branch directory.

for RUNTEST_ARGS in \
    "systemui" \
    "--path frameworks/base/services/tests/servicestests/src/com/android/server/notification"
do
    echo "Running runtest $RUNTEST_ARGS ..."
    RESULT=$(development/testrunner/runtest.py $RUNTEST_ARGS | tee /dev/tty)
    SUCCESS_PATTERN="OK \([0-9]+ tests\)"
    if [[ ! $RESULT =~ $SUCCESS_PATTERN ]]
    then
        echo -e "\nThere were one or more FAILURES above";
        exit 1
    fi
done

echo -e "\nOK!  All SystemUI tests passed! :)"
