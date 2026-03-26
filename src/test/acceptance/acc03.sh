#!/usr/bin/env bash
echo -n "[04-GVT][$0] starting... "
cd my_repo
if [[ $(java -jar ../build/libs/great-versioning-tool-1.0.jar init) = "Current directory initialized successfully." ]]; then
    echo "pass init"
else
    cd -
    echo "fail init"
    exit 1
fi

cd -
