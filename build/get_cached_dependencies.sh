#!/bin/bash
cd $HOME/.m2
aws s3 cp s3://exwhy-dependencies/cached.tar.bz2 cached-dependencies.tar.bz2
tar -xjf cached-dependencies.tar.bz2