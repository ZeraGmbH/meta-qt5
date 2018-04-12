inherit qmake5_base

DEPENDS_prepend = "qtbase-native qbs-native "

do_configure() {
}

# additional build settings e.g 
#    foo.prefix:${prefix}
#    '--log-level debug'
EXTRA_OECONF_QBS ??= ""

QBS_DATA_PATH ??= "${datadir}/qbs"

QT5_BIN_PATH = "${STAGING_DIR_NATIVE}${OE_QMAKE_PATH_HOST_BINS}"
QT5_BIN_PATH_class-native = "${OE_QMAKE_PATH_HOST_BINS}"

# !!!!! Dont't forget '--settings-dir ${B}/qbs' otherwise local !!!!!
# !!!!! host's defaults are changed                             !!!!!
do_configure() {
    # we need qt5 bin paths in search path
    export PATH="${PATH}:${QT5_BIN_PATH}"

    # REVISIT fo clang ??
    qbs setup-toolchains --settings-dir ${B}/qbs --type gcc "`echo ${CXX} | awk '{ print $1 }'`" gcc
    qbs setup-qt         --settings-dir ${B}/qbs ${STAGING_BINDIR_NATIVE}${QT_DIR_NAME}/qmake qt5
    qbs config           --settings-dir ${B}/qbs profiles.qt5.baseProfile gcc
    qbs config           --settings-dir ${B}/qbs preferences.qbsSearchPaths ${STAGING_DIR_HOST}${QBS_DATA_PATH}
}

do_compile() {
    # we need qt5 bin paths in search path
    export PATH="${PATH}:${QT5_BIN_PATH}"

    # for rebuild
    qbs resolve          --settings-dir ${B}/qbs -f ${S} -d ${B} profile:qt5 ${EXTRA_OECONF_QBS}
    qbs build            --settings-dir ${B}/qbs -f ${S} -d ${B} --no-install profile:qt5 ${EXTRA_OECONF_QBS}
}

do_install() {
    # we need qt5 bin paths in search path
    export PATH="${PATH}:${OE_QMAKE_PATH_HOST_BINS}"

    qbs install --settings-dir ${B}/qbs -d ${B} --no-build -v --install-root ${D} profile:qt5
}
