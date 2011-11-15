#!/usr/bin/env sh
./playgame.py -e -So --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 60 --map_file maps/example/tutorial1.map "$@" \
	"python sample_bots/python/HunterBot.py" | java -jar visualizer.jar
