# JuCify Docker Image

Notice: there are some modifications based on the author's code, including bug fixes and timeout printing.

This version is based on commit `6897c58350773987da0a2c52a99fd4ebf1edc39e` by following the instruction on [README](https://github.com/NativeSummary/JuCify/blob/de4cd20ceb143d1453af52886005fbdf6c988ed8/README.md)


### Docker image

- mount platforms directory into container path `/platforms`  (from android sdk installation or download from https://github.com/Sable/android-platforms)
- mount folder containing apk into container path `/root/apps`
- default entrypoint is `/root/JuCify/runTool.sh` (usage see `JuCify\scripts\main.sh`), specify mounted platforms dir after `-p`, full apk path after `-f`(can't use relative path). specify `-t` for taint analysis.
- intermediate files and folders include `APK_NAME/ APK_NAME_result/ APK_NAME.native.log APK_NAME.flow.log`. specify `-c` in cmdline to automatically delete some folders.

for example:
```
docker run --rm -v /path/to/platforms:/platforms -v /path/to/benchApps:/root/apps nativesummary/jucify -p /platforms -f /root/apps/getter_imei.apk -t -c
```

or override entrypoint to execute preferred script
```
docker run --rm -v /path/to/platforms:/platforms -v /path/to/benchApps:/root/apps --entrypoint /bin/bash nativesummary/jucify /root/JuCify/runTool.sh -p /platforms -f /root/apps/getter_imei.apk -t -c
```

**configurable timeout environment variables:**
- `JUCIFY_TIMEOUT` timout for the whole analysis.
- `BINARY_TIMEOUT`(scripts/execute_with_limit_time.sh): timeout for the whole nativediscloser angr analysis.
- `WAIT_TIME`(nativediscloser/main.py): timeout for each JNI method.
- `DYNAMIC_ANALYSIS_TIME`: timeout for `JNI_OnLoad`
- `MAX_LENGTH`: limit the length of symbolic execution

**nativediscloser submodule is modified**, see commits in: https://github.com/nativesummary/nativediscloser/
- install `angr==8.20.7.6 androguard==3.3.5 pygraphviz==1.7 protobuf==3.20.1 cle==8.20.7.6`
- patch angr bug at this version: `sed -i "s/reloc.relocate(\[\])/reloc.relocate()/" /home/user/.local/lib/python3.10/site-packages/angr/project.py`
    - see https://github.com/angr/angr/blame/ef275c616f39e66ea5b865f825627c010e7fbec8/angr/project.py#L525

**Sources and Sinks** 
- Default sources and sinks in the docker image is from taintbench's verbose [sources](https://github.com/TaintBench/TaintBench/blob/master/merged_sources.txt) and [sinks](https://github.com/TaintBench/TaintBench/blob/master/merged_sinks.txt).
- Sadly, it is embedded in jar. to modify:
    1. change src/main/resources/SourcesSinks.txt
    1. recompile and rebuild docker image

# JuCify

Unifying Android code for enhanced static analysis.

## Getting started

### Downloading the tool

<pre>
git clone https://github.com/JordanSamhi/JuCify.git
</pre>

### Installing the tool

<pre>
cd JuCify
mvn clean install
</pre>

### Issues

If you stumble upon a stack overflow error while building JuCify, increase memory available with this command:

<pre>
export MAVEN_OPTS=-Xss32m
</pre>

Then, try to rebuild.

### Using the tool

<pre>
java -jar JuCify/target/JuCify-0.1-jar-with-dependencies.jar <i>options</i>
</pre>

Options:

* ```-a``` : The path to the APK to process.
* ```-p``` : The path to Android platofrms folder.
* ```-f``` :  Provide paths to necessary files for native reconstruciton.
* ```-r``` : Print raw results.
* ```-ta``` : Perform taint analysis.
* ```-c``` : Export call-graph to text file.
* ```-e``` : Export call-graph to dot format.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details

## Contact

For any question regarding this study, please contact us at:
[Jordan Samhi](mailto:jordan.samhi@uni.lu)
