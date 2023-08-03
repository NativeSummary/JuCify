export MAVEN_OPTS=-Xss32m
mvn clean install:install-file -Dfile=libs/soot-infoflow-android-classes.jar -DgroupId=de.tud.sse -DartifactId=soot-infoflow-android -Dversion=2.7.1 -Dpackaging=jar
mvn clean install:install-file -Dfile=libs/soot-infoflow-classes.jar -DgroupId=de.tud.sse -DartifactId=soot-infoflow -Dversion=2.7.1 -Dpackaging=jar
mvn clean install

docker image rm jucify && \
docker build . --tag jucify --build-arg UBUNTU_MIRROR=mirrors.ustc.edu.cn --build-arg PYTHON_MIRROR=pypi.tuna.tsinghua.edu.cn
