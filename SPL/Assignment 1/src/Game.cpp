#include "Game.h"
#include <string>
#include <iostream>

int _N;
int verbal;

Game::Game(char* configurationFile) : listPlayers(), gameDeck(), initialDeckString(), winners(), turnCount()
{
	winners.resize(0);
	ifstream configfile(configurationFile);
	string line;
	int nextParameter = 1;
	int newPlayerStrategy;
	string newPlayerName;
	while (getline(configfile, line))
	{
		if (line.empty() || line.front() == '#' || line.size() == 0)
			continue;
		while (!(line.empty()) && (line.back() == ' ' || line.back() == '\r')) { line.pop_back(); }
		if (line.empty() || line.front() == '#' || line.size() == 0)
			continue;
		switch (nextParameter)
		{
			case 1: { verbal = stoi(line.substr(0, 1)); nextParameter++; } break;
			case 2: { _N = stoi(line); nextParameter++; } break;
			case 3: { initialDeckString = line; nextParameter++; } break;
			case 4: 
			{
				newPlayerName = line.substr(0, line.find(' '));
				newPlayerStrategy = stoi(line.substr(line.find(' ')+1));
				addPlayer(newPlayerName, newPlayerStrategy);
			} break;
		}
		

	}
}
Game::Game(const Game& other) : listPlayers(), gameDeck(), initialDeckString(), winners(), turnCount()
{
	turnCount = other.turnCount;
	initialDeckString = other.initialDeckString;

	gameDeck = other.gameDeck;
	listPlayers = other.listPlayers;
	for (size_t i = 0; i < other.listPlayers.size(); i++)
	{
		listPlayers[i] = other.listPlayers[i]->clone();
	}
	winners = other.winners;
	for (size_t i = 0; i < other.winners.size(); i++)
	{
		winners[i] = other.winners[i]->clone();
	}
}

void Game::addPlayer(string name, int strategy)
{
	switch (strategy)
	{
	case 1: listPlayers.push_back(new PlayerType1(name, listPlayers.size())); break;
	case 2: listPlayers.push_back(new PlayerType2(name, listPlayers.size())); break;
	case 3: listPlayers.push_back(new PlayerType3(name, listPlayers.size())); break;
	case 4: listPlayers.push_back(new PlayerType4(name, listPlayers.size())); break;
	}
}
void Game::init()
{
	gameDeck = Deck(initialDeckString);
	deal(&listPlayers);
}

bool Game::deal(vector<Player*>* playerVec)
{
	for (size_t i = 0; i < playerVec->size(); i++)
		for (int j = 0; j < 7; j++)
		{
			Card* toAdd = gameDeck.fetchCard();
			if (!(*playerVec)[i]->addCard(*toAdd))
				delete toAdd;
		}
		return true;
}

void Game::play()
{
	bool gotAWinner = false;
	turnCount = 0;
	while (gotAWinner == false)
	{
		for (size_t i = 0; i < listPlayers.size() && (gotAWinner == false); i++)
		{
			turnCount++;
			if (verbal == 1) { std::cout << endl << "Turn " << turnCount << std::endl; }
			if (verbal == 1) { printState(); }
			listPlayers[i]->playTurn(listPlayers, &gameDeck);
			gotAWinner = checkForWinners();
		}
	}
}

bool Game::checkForWinners()
{
	for (size_t i = 0; i < listPlayers.size(); i++)
	{
		if (listPlayers[i]->getNumberOfCards() == 0)
			winners.push_back(listPlayers[i]);
	}
	if (winners.size() > 0)
		return true;
	else
		return false;
}

void Game::printState()
{
	std::cout << "Deck: " << gameDeck.toString() << std::endl;
	for (size_t i = 0; i < listPlayers.size(); i++)
	{
		std::cout << listPlayers[i]->getName() << ": " << listPlayers[i]->toString() << std::endl;
	}
}
void Game::printWinner()
{
	if (winners.size() == 1)
	{
		std::cout << "***** The Winner is: " << winners[0]->getName() << " *****" << std::endl;
	}
	else if (winners.size() == 2)
	{
		std::cout << "***** The winners are: " << winners[0]->getName() << " and " << winners[1]->getName() << " *****" << std::endl;
	}
	else std::cout << "INVALID NUMBER OF WINNERS" << std::endl;
	
}

void Game::printNumberOfTurns()
{
	std::cout << "Number of turns: " << turnCount << std::endl;
}

Game::~Game() {
	for (size_t i = 0; i < listPlayers.size(); i++)
	{
		delete listPlayers[i];
	}
	listPlayers.clear();

}