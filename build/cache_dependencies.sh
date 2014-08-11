tar -cjf cached-dependencies.tar.bz2 $HOME/.m2
aws s3 cp cached-dependencies.tar.bz2 s3://exwhy-dependencies/cached.tar.bz2