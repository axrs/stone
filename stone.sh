#!/usr/bin/env bash
cd $(realpath $(dirname $0))

if [ ! -f ./project.sh ]; then
	echo "Downloading bash helper utilities"
	curl -OL https://raw.githubusercontent.com/jesims/backpack/master/project.sh
fi

source ./project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

ensure_githooks () {
	local githooks_folder="githooks"
	if [ "$(git config core.hooksPath)" != "$githooks_folder" ];then
		echo "Setting up GitHooks"
		git config core.hooksPath "$githooks_folder"
	fi
}

shadow-cljs () {
	lein trampoline run -m shadow.cljs.devtools.cli $@
}

## clean:
## Cleans the project of all compiled/generated sources
clean () {
	echo_message "Cleaning generated folders and files"
	lein clean
	rm -rf .shadow-cljs/builds/* .cpcache target/*
}

## deps:
## Installs all necessary dependencies
deps () {
	echo_message "Installing dependencies"
	npm install
	abort_on_error
	lein deps
	abort_on_error
}

## format:
## Formats all project source files in a consistent manner
format () {
	lein cljfmt fix
	abort_on_error
    npx remark . --use remark-preset-lint-recommended --use toc --use bookmarks -o
    abort_on_error
}

## lint:
## Runs various linters over different project files to check for consistency
lint () {
	echo_message "Linting"
	lein cljfmt check
	abort_on_error
}

unit-test-once (){
	echo_message "Unit Testing"
	shadow-cljs compile test
	abort_on_error
	npx karma start --single-run
	abort_on_error
}

unit-test-refresh () {
	echo_message "Unit Testing (refreshing)"
	shadow-cljs watch test-browser
	abort_on_error
}

## unit-test:
## args: [-r]
## Runs the ClojureScript unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
unit-test () {
	clean
	local flag=${1:-""}
	case ${flag} in
		-r)
			unit-test-refresh;;
		*)
			unit-test-once;;
	esac
}

is-snapshot () {
	version=$(cat VERSION)
	[[ "$version" == *SNAPSHOT ]]
}

deploy () {
	lein deploy clojars
	abort_on_error
}

## snapshot:
## Pushes a snapshot to Clojars
snapshot () {
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		version=$(cat VERSION)
		snapshot="$version-SNAPSHOT"
		echo ${snapshot} > VERSION
		echo_message "Snapshotting $snapshot"
		deploy
		echo "$version" > VERSION
	fi
}

## release:
## Pushes a release to Clojars
release () {
	version=$(cat VERSION)
	if ! is-snapshot;then
		version=$(cat VERSION)
		echo_message "Releasing $version"
		deploy
	else
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	fi
}

ensure_githooks

if [[ "$#" -eq 0 ]] || [[ "$1" =~ ^(help|-h|--help)$ ]];then
	usage
	exit 1
elif [[ $(grep "^$1\ (" "$script_name") ]];then
	eval $@
else
	echo_error "Unknown function $1 ($script_name $@)"
	exit 1
fi
