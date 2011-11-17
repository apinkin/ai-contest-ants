#!/usr/bin/env sh

#./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 100 --map_file tools/maps/example/tutorial1.map "$@" "python sample_bots/python/HunterBot.py" --verbose -e

./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 200 --map_file tools/maps/maze/maze_1.map "$@" "java -jar ../archive/rebelxt01.jar" --verbose -e
