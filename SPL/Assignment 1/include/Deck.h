#ifndef DECK_H_
#define DECK_H_

#include <iostream>
#include <algorithm>
#include <fstream>
#include <vector>
#include <string>

#include "Card.h"

class Deck {
private:
	int const _CARDS_TO_DEAL = 7;
	vector<Card*> deck;
public:
	Deck();
	Deck(string deckAsString);
	Deck& operator=(const Deck&);
	Card* fetchCard();   //Returns the top card of the deck and remove it rom the deck
	int getNumberOfCards(); // Get the number of cards in the deck
	string toString(); // Return the cards in top-to-bottom order in a single line, cards are separated by a space ex: "12S QD AS 3H"
	Shape charToShape(char);
	Figure charToFigure(char) const;
	virtual ~Deck();
};

#endif
