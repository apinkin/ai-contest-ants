#!/usr/bin/env sh
./make.sh

./playgame.py --player_seed 42 --end_wait=0.25 --verbose --log_dir game_logs --turns $2 --map_file "$1" "java -jar ../build/MyBot.jar" "python sample_bots/python/GreedyBot.py" --verbose -e


