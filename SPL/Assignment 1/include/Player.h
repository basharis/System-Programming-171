#ifndef PLAYER_H_
#define PLAYER_H_

#include <iostream>

#include "Hand.h"
#include "Deck.h"

using namespace std;

class Player : public Hand {
private:
	const string name;
public:
	Player(string);
	Player(string, vector<Card*>);
	string getName() const;   //Returns the name of the player
	virtual void playTurn(const vector<Player*>&, Deck*) = 0;
	void ask(Player*, int, Deck*);
	const int whoHasMostCards(const vector<Player*>&, int);
	virtual ~Player();
	virtual Player* clone() const = 0 ;
};

class PlayerType1 : public Player {  //For strategy 1
private:
	int playerToAsk;
	int myPosition;
public:
	PlayerType1(string,int);
	PlayerType1(string, int, vector<Card*>);
	int haveMost();
 	void playTurn(const vector<Player*>&, Deck*) override;
	PlayerType1* clone() const override;
	virtual ~PlayerType1();
};

class PlayerType2 : public Player {  //For strategy 2
private:
	int playerToAsk;
	int myPosition;
public:
	PlayerType2(string,int);
	PlayerType2(string, int, vector<Card*>);
	int haveLeast();
	void playTurn(const vector<Player*>&,Deck*) override;
	virtual ~PlayerType2();
	PlayerType2* clone() const override;
};

class PlayerType3 : public Player {  //For strategy 3
private:
	int playerToAsk;
	int myPoisition;
public:
	PlayerType3(string,int);
	PlayerType3(string, int, vector<Card*>);
	int highestValue();
	void playTurn(const vector<Player*>&,Deck*) override;
	virtual ~PlayerType3();
	PlayerType3* clone() const override;
};

class PlayerType4 : public Player {  //For strategy 4
private:
	int playerToAsk;
	int myPoisition;
public:
	PlayerType4(string,int);
	PlayerType4(string, int, vector<Card*>);
	int lowestValue();
	void playTurn(const vector<Player*>&,Deck*) override;
	PlayerType4* clone() const override;
	virtual ~PlayerType4();
};

#endif
