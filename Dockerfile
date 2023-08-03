FROM ubuntu:18.04

# clean files in .gitignore before building image

# develop: docker run -it --name test_jucify -v C:\JuCify\:/root/Jucify ubuntu:22.04
# headless jdk saves 300mb space
# "build-essential python3.7-dev graphviz libgraphviz-dev" is required for pygraphviz
SHELL ["/bin/bash", "-c"]

# docker build . --tag jucify --build-arg UBUNTU_MIRROR=mirrors.ustc.edu.cn --build-arg PYTHON_MIRROR=pypi.tuna.tsinghua.edu.cn
ARG UBUNTU_MIRROR
# =mirrors.tuna.tsinghua.edu.cn
ARG PYTHON_MIRROR
# =pypi.tuna.tsinghua.edu.cn

RUN if [[ ! -z "$UBUNTU_MIRROR" ]] ; then sed -i "s/archive.ubuntu.com/$UBUNTU_MIRROR/g" /etc/apt/sources.list \
 && sed -i "s/security.ubuntu.com/$UBUNTU_MIRROR/g" /etc/apt/sources.list ; fi ; \
 apt update && DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends time git wget sudo software-properties-common nano maven openjdk-17-jdk-headless python3.7 python3.7-venv python3-pip python3.7-dev build-essential graphviz libgraphviz-dev unzip \
 && if [[ ! -z "$PYTHON_MIRROR" ]] ; then python3 -m pip config set global.index-url https://$PYTHON_MIRROR/simple ; fi ; \
 apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

WORKDIR /root

# Python env
RUN python3.7 -m venv jucify_venv \
 && echo source /root/jucify_venv/bin/activate >> /root/.bashrc \
 && source /root/jucify_venv/bin/activate \
 && python3.7 -m pip install -U pip wheel \
 && python3.7 -m pip install angr==8.20.7.6 androguard==3.3.5 pygraphviz==1.7 protobuf==3.20.1 cle==8.20.7.6 \
 && sed -i "s/reloc.relocate(\[\])/reloc.relocate()/" /root/jucify_venv/lib/python3.7/site-packages/angr/project.py


COPY ["runTool.sh", "/root/JuCify/"]
COPY ["nativediscloser", "/root/JuCify/nativediscloser"]
COPY ["scripts", "/root/JuCify/scripts"]
COPY ["target/JuCify-0.1-jar-with-dependencies.jar", "/root/JuCify/target/"]


# build jucify jar
# WORKDIR /root/JuCify
# RUN /root/JuCify/build.sh

# /root/JuCify/runTool.sh
ENTRYPOINT ["/bin/bash", "/root/JuCify/runTool.sh"]
CMD ["-p", "/platforms", "-f", "/root/apps/app.apk", "-t", "-c"]
