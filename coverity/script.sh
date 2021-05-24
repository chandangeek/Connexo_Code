if [[ -z "${TARGET_DIR}" ]]; then
  COV_DIR="--dir target/idir"
else
  COV_DIR="--dir ${TARGET_DIR}/idir"
fi

KEY_FILE=coverity/ak-coverity.swtools.honeywell.com-8443
BIN_DIR="$COVERITY_TOOL_HOME/bin"

chmod 600 $KEY_FILE

echo "Coverity is using environment variable STREAM set to $STREAM"
echo "Coverity is putting output in $COV_DIR"
set -x

$BIN_DIR/cov-capture $COV_DIR --config coverity/coverity_config.xml --source-dir .
$BIN_DIR/cov-manage-emit $COV_DIR --config coverity/coverity_config.xml --tu-pattern "file('/sencha.ext/')" delete
$BIN_DIR/cov-manage-emit $COV_DIR --config coverity/coverity_config.xml --tu-pattern "file('/js.vendor/')" delete
$BIN_DIR/cov-import-scm $COV_DIR --config coverity/coverity_config.xml --scm git
$BIN_DIR/cov-analyze --jobs max4 $COV_DIR --config coverity/coverity_config.xml

$BIN_DIR/cov-commit-defects $COV_DIR --url https://coverity.swtools.honeywell.com:8443 --auth-key-file $KEY_FILE --stream $STREAM
