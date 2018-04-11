SUMMARY = "Modern build tool for software projects"
HOMEPAGE = "http://wiki.qt.io/Qbs"
LICENSE = "LGPLv2.1+ & The-Qt-Company-Qt-LGPL-Exception-1.1"
LIC_FILES_CHKSUM = " \
    file://LGPL_EXCEPTION.txt;md5=f4748b0d1a72c5c8fb5dab2dd1f7fa46 \
    file://LICENSE.GPL3-EXCEPT;md5=763d8c535a234d9a3fb682c7ecb6c073 \
    file://LICENSE.LGPLv3;md5=466a5dbc4996e12165deb9b21185e2ad \
    file://LICENSE.LGPLv21;md5=243b725d71bb5df4a1e5920b344b86ad \
"

inherit qmake5

DEPENDS = " \
    qemu-native \
    python-beautifulsoup4 \
    python-lxml \
    help2man-native \
    qtscript \
"
DEPENDS_append_class-target = " \
    ${BPN}-native \
"

SRC_URI = " \
    git://code.qt.io/qbs/qbs.git \
    file://0001-Enable-cross-compiling.patch \
    file://0002-Do-not-kill-our-link-flags.patch \
"

SRC_URI_append_class-native = " \
    file://0003-ProcessCommandExecutor-remove-absolute-paths.patch \
"

SRCREV = "0b141d7ecb103d53cc3ea93db505400fe29b46e5"
S = "${WORKDIR}/git"

QMAKE_PROFILES = "${S}/qbs.pro"
EXTRA_QMAKEVARS_PRE += " \
    QBS_INSTALL_PREFIX=${prefix} \
    QBS_APPS_INSTALL_DIR=${OE_QMAKE_PATH_BINS} \
"
# qbs itself does not find plugins with this!
#    QBS_PLUGINS_INSTALL_DIR=${OE_QMAKE_PATH_PLUGINS} 


# If somebody knows a better solutition to remove abs paths in native context
# let me know :)
def prune_native_prefix(d, path):
    to_prune = d.getVar("STAGING_DIR_NATIVE")
    return path.replace(to_prune, "")


QBS_EXECPATHS = "${@prune_native_prefix(d,'${OE_QMAKE_PATH_HOST_BINS}')}:${@prune_native_prefix(d,'${bindir}')}:${@prune_native_prefix(d,'${base_bindir}')}"

do_configure_append_class-native() {
    # make absolute path removal work -
    # see 0003-ProcessCommandExecutor-remove-absolute-paths.patch
    sed -i 's|%OE_BIN_PATHS%|${QBS_EXECPATHS}|g' ${S}/src/lib/corelib/buildgraph/processcommandexecutor.cpp
}

do_configure_append_class-target() {
    # We cannot use cross qbs to configure initial files
    sed -i 's|bin/qbs|qbs|g' ${B}/Makefile.static-res
}

do_install_append_class-target() {
    # do not install unnecessary python build modules. These are required for
    # macOS only:
    # https://bugzilla.redhat.com/show_bug.cgi?id=1559529
    # https://src.fedoraproject.org/cgit/rpms/qbs.git/tree/qbs.spec

    rm -f ${D}${libexecdir}/qbs/dmgbuild
    rm -rf ${D}${datadir}/qbs/python
}

FILES_${PN} += " \
    ${OE_QMAKE_PATH_PLUGINS} \
"

FILES_${PN}-dev += " \
    ${OE_QMAKE_PATH_LIBS}/*.prl \
"

BBCLASSEXTEND = "native"
