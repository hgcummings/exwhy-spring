#!/bin/bash
ls $HOME/.m2/
mkdir dependencies
cd dependencies
tar -cjfv cached.tar.bz2 $HOME/.m2/