#ifndef HAND_H_
#define HAND_H_

#include <vector>
#include <iostream>
#include <algorithm>
#include "Card.h"

using namespace std;

class Hand {
private:
	vector<Card*> hand;
public:
	Hand();
	Hand(vector<Card*> newHand);
	Hand& operator=(const Hand&);
	Figure charToFigure(char) const;
	bool addCard(Card&);
	bool removeCard(Card&);
	bool removeCard(Card&, int);
	bool Squeeze(int);
	int getCardIndex(Card&);
	Card* getCardByIndex(int);
	int* Search(int cardNumericRep);
	int getNumberOfCards(); // Get the number of cards in hand
	string toString(); // Return a list of the cards, separated by space, in one line, in a sorted order, ex: "2S 5D 10H"
	int mostInHand();
	int leastInHand();
	int highestInHand();
	int lowestInHand();
	virtual ~Hand();
	vector<Card*> cloneHand() const;
};

#endif
