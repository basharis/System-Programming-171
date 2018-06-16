#ifndef GAME_H_
#define GAME_H_

#include <iostream>
#include <fstream>
#include <string>
#include <vector>

#include "Player.h"
#include "Deck.h"
#include "Global.h"

using namespace std;

class Game {
private:
	vector<Player*> listPlayers;   //The list of players
	Deck gameDeck;                 //The deck of the game
	string initialDeckString;
	vector<Player*> winners;
	int turnCount;
public:
	Game(char* configurationFile);
	Game(const Game& other);
	virtual ~Game();
	void addPlayer(string, int);
	void init();
	bool deal(vector<Player*>*); // Tamir: DEAL CARDS TO PLAYERS.
	void play();
	bool checkForWinners();
	void printState();        //Print the state of the game as described in the assignment.
	void printWinner();       //Print the winner of the game as describe in the assignment.
	void printNumberOfTurns(); //Print the number of played turns at any given time.
};

#endif
