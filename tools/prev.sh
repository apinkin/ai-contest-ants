#!/usr/bin/env sh
./make.sh

./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 200 --map_file "$@" "java -jar ../build/MyBot.jar" "java -jar ../archive/rebelxt16.jar" --verbose -e

#./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 200 --map_file "$@" "java -jar ../archive/rebelxt02.jar" "java -jar ../archive/rebelxt02.jar" --verbose -e

