# Statsd Configs

This directory hosts binary statsd config protos.

## What they do

A config tells statsd what metrics to collect from the device during a test. For example,
`app-start` will instruct statsd to collect app startup metrics.

## Checking in a config

To check in a config, follow these steps:

1. Create a directory under this directory for the new config (e.g. `app-start`).
2. Put the new config in the subdirectory using the directory name + `.pb` extension.
3. Write a README file explaining what the config does and put it under the new subdirectory.
