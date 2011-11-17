#!/usr/bin/env sh
./make.sh

./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns 200 --map_file "$@" "java -jar ../build/MyBot.jar" "python sample_bots/python/GreedyBot.py" "python sample_bots/python/HunterBot.py" --verbose -e


