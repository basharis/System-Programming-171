# make all compile all
all: makereviiyot


makereviiyot: bin/Card.o bin/Deck.o bin/Hand.o bin/Player.o bin/Game.o bin/reviiyot.o
		g++ -o bin/reviiot bin/Card.o bin/Deck.o bin/Hand.o bin/Player.o bin/Game.o bin/reviiyot.o

bin/Card.o: src/Card.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Card.o src/Card.cpp

bin/Deck.o: src/Deck.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Deck.o src/Deck.cpp

bin/Hand.o: src/Hand.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Hand.o src/Hand.cpp

bin/Player.o: src/Player.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Player.o src/Player.cpp

bin/Game.o: src/Game.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Game.o src/Game.cpp

bin/reviiyot.o: src/reviiyot.cpp
		g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/reviiyot.o src/reviiyot.cpp

clean:
	rm -f bin/*
