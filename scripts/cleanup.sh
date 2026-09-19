#!/bin/bash

set -e

docker compose -p friends down --remove-orphans -v
