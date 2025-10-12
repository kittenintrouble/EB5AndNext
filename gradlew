#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support functions.
# Unset irrelevant variables.
unset GREP_OPTIONS

# Determine the script's location.
PRG="$0"
# Need to resolve symlinks before we can cd to the script's directory.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
 done
SAVED=`pwd`
cd "`dirname "$PRG"`/" >/dev/null
APP_HOME=`pwd -P`
cd "$SAVED" >/dev/null

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Increase the maximum file descriptors if we can.
if [ "$MAX_FD" != "maximum" -a "$MAX_FD" != "" ] ; then
    ulimit -n $MAX_FD
    if [ $? -ne 0 ] ; then
        warn "Could not set maximum file descriptor limit: $MAX_FD"
    fi
else
    ulimit -n 8192 >/dev/null 2>&1
fi

# For Darwin, add options to specify the Mac menu name.
if [ "`uname`" = "Darwin" ] ; then
    GRADLE_OPTS="$GRADLE_OPTS -Xdock:name=$APP_NAME -Xdock:icon=$APP_HOME/media/gradle.icns"
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
# from http://unixhelp.ed.ac.uk/shell/bash.html
JAVACMD_BASENAME=`basename "$JAVACMD"`
case "$JAVACMD_BASENAME" in
    *sh|*ksh)
        # Handle shell wrappers specially because they need to read and evaluate Gradle options
        # within their own shell process. Eval is used to write the final command line because sh/ksh
        # do not support processing into a variety of parameter words.
        eval "set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \"-classpath\" \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain \"$@\""
        exec "$JAVACMD" "$@"
        ;;
    *)
        exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
        ;;
esac
