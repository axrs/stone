#!/usr/bin/env bash
cd $(realpath $(dirname $0))
set -euo pipefail

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
	lein deps
}

## format:
## Formats all project source files in a consistent manner
format () {
	lein cljfmt fix
    npx remark . --use remark-preset-lint-recommended --use toc --use bookmarks -o
}

## lint:
## Runs various linters over different project files to check for consistency
lint () {
	echo_message "Linting"
	lein cljfmt check
}

unit-test-once (){
	echo_message "Unit Testing"
	npx shadow-cljs compile test
	npx karma start --single-run
}

unit-test-refresh () {
	echo_message "Unit Testing (refreshing)"
	npx shadow-cljs watch test-browser
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
