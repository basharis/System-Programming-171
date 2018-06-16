#include "Deck.h"

Deck::Deck() : deck() {}
Deck::Deck(string deckAsString) : deck()
{
	Shape newCardShape;
	string cardAsString;
	while (deckAsString.back() == ' ') { deckAsString.pop_back(); }
	while (deckAsString.size() > 0)
	{
		cardAsString = deckAsString.substr(0, deckAsString.find(' '));

		newCardShape = charToShape(cardAsString.back());
		cardAsString.pop_back();
		if (isdigit(cardAsString.front())) {
			deck.push_back(new NumericCard(stoi(cardAsString), newCardShape));
		}
		else
			deck.push_back(new FigureCard(charToFigure(cardAsString.front()), newCardShape));
		if (deckAsString.find(' ') != string::npos) {
			deckAsString = deckAsString.substr(deckAsString.find(' ') + 1);
		}
		else
			deckAsString = "";
	}
}
Deck& Deck::operator=(const Deck& other)
{
	deck.resize(other.deck.size());
	for (size_t i = 0; i < deck.size(); i++)
	{
		deck[i] = other.deck[i]->clone();
	}
	return *this;
}


Card* Deck::fetchCard()
{
	Card* fetchedCard;
	fetchedCard = deck[0];
	deck.erase(deck.begin());
	return fetchedCard;	
}

int Deck::getNumberOfCards()
{
	return deck.size();
}

string Deck::toString()
{
	string deckToString = "";
	for (int i = 0; i < static_cast<int>(deck.size()); i++)
	{
		deckToString += deck[i]->toString() + " ";
	}
	if (deckToString.size() > 0)
		deckToString.pop_back();
	return deckToString;
}

Shape Deck::charToShape(char charToParse)
{
	if (charToParse == 'C') return Club;
	if (charToParse == 'D') return Diamond;
	if (charToParse == 'H') return Heart;
	if (charToParse == 'S') return Spade;

	return Club;

}

Figure Deck::charToFigure(char charToParse) const
{
	if (charToParse == 'J') return Jack;
	if (charToParse == 'Q') return Queen;
	if (charToParse == 'K') return King;
	if (charToParse == 'A') return Ace;

	return Jack;

}

Deck::~Deck()
{
	for (size_t i=0 ; i<deck.size() ; i++)
	{
		delete deck[i];
	}
	deck.clear();
}
