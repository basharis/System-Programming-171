#include "Hand.h"


Hand::Hand() : hand({})
{
}
Hand::Hand(vector<Card*> newHand) : hand(newHand)
{
}

Hand& Hand::operator=(const Hand& other)
{
	hand = other.hand;
	for (size_t i = 0; i < hand.size(); i++)
	{
		hand[i] = other.hand[i]->clone();
	}
	return *this;
}

bool Hand::addCard(Card& card)
{
	int brotherCards = 0;
	int brotherCardsArray[3];
	bool foundPlace = false;
	int addedCardIndex=0;
	double myNumRep = card.getNumRepresentation(); 
	int i = 0;
	for (i = 0; (i < (int)(hand.size())) && !foundPlace; i++)
	{
		double myfriend = (hand[i])->getNumRepresentation();
		if ((myNumRep < myfriend) || ((myNumRep > myfriend) && (i == (int) hand.size() - 1))) {
			if ((myNumRep > myfriend) && (i == (int) hand.size() - 1))
				i++;
			foundPlace = true;
			for (int j = max(i - 3, 0); j < min(i + 4, (int) hand.size()); j++)
			{
				if ((int) myNumRep == (int) (hand[j]->getNumRepresentation()))
				{
					brotherCards++;
					brotherCardsArray[brotherCards - 1] = j;
				}
			}
		}
	}
	if (foundPlace)
		addedCardIndex = i - 1;
	else
		addedCardIndex = i;
	if (brotherCards == 3)
	{
		removeCard(*hand[brotherCardsArray[2]], brotherCardsArray[2]);
		removeCard(*hand[brotherCardsArray[1]], brotherCardsArray[1]);
		removeCard(*hand[brotherCardsArray[0]], brotherCardsArray[0]);
		return false; // card not added - completed a quaretet
	}
	else
	{
		hand.resize(hand.size() +1);
		for (int i = hand.size()-1 ; i > addedCardIndex ; i--)
		{
			hand[i] = hand[i - 1];
		}
		hand[addedCardIndex] = &card;
		return true; // card added to hand
	}
}

bool Hand::removeCard(Card& card)
{
	int index = getCardIndex(card);
	if (index == -1)
		return false;
	else 
		return Squeeze(index);
}

bool Hand::removeCard(Card& card, int index)
{
	if (hand[index]->getNumRepresentation() == card.getNumRepresentation())
	{
		Squeeze(index);
		delete &card;
		return true; 
	}
	else
		return false;
}

bool Hand::Squeeze(int index)
{
	for (size_t i = index + 1; i < hand.size(); i++)
		hand[i - 1] = hand[i];
	hand.resize(hand.size() - 1);
	return true;
}

int Hand::getCardIndex(Card& card)
{
	for (size_t i = 0; i < hand.size(); i++)
	{
		if (hand[i]->getNumRepresentation() == card.getNumRepresentation())
			return i;
	}
	return -1;
}

Card* Hand::getCardByIndex(int index)
{
	return hand[index];
}

int* Hand::Search(int cardNumericRep)
{
	int *foundCards = new int[3];
	foundCards[0] = -1; foundCards[1] = -1; foundCards[2] = -1;
	int numCardsFound = 0;
	for (size_t i = 0; i < hand.size(); i++)
		if (cardNumericRep == (int) hand[i]->getNumRepresentation())
		{
			foundCards[numCardsFound] = i;
			numCardsFound++;
		}
	return foundCards;
}

int Hand::getNumberOfCards()
{
	return hand.size();
}

string Hand::toString()
{
	string handString = "";
	for (size_t i = 0; i < hand.size(); i++)
	{
		handString += hand[i]->toString() + " ";
	}
	if(hand.size() > 0)
		handString.pop_back();
	return handString;
}

int Hand::mostInHand() 
{ 
	int times = 0; 
	int maxTimes = 0; 
	int haveMost;
	int prevCard = (int) hand[0]->getNumRepresentation();
	for (size_t i = 0; i < hand.size(); i++)
	{
		if (prevCard == (int)hand[i]->getNumRepresentation())
		{
			times++;
		}
		else
		{
			times = 1;
			prevCard = (int)hand[i]->getNumRepresentation();
		}
		if (times >= maxTimes)
		{
			haveMost = prevCard;
			maxTimes = times;
		}
	}
	return haveMost;
}

int Hand::leastInHand() 
{
	int haveLeast;
	int times = 0;
	int minTimes = 4;
	int currCard;
	for (size_t i = 0; i < hand.size(); i++)
	{
		currCard = (int) hand[i]->getNumRepresentation();
		times = 1;
		if ((i + 1) < hand.size())
		{
			if (currCard == (int) hand[i + 1]->getNumRepresentation())
			{
				times++;
				i++;
				if ((i + 1) < hand.size())
				{
					if (currCard == (int) hand[i + 1]->getNumRepresentation())
					{
						times++;
						i++;
					}
				}
			}
		}
		if (times < minTimes)
		{
			minTimes = times;
			haveLeast = currCard;
		}
	}
	return haveLeast;
}

int Hand::highestInHand()
{
	return (int) (hand.back())->getNumRepresentation();
}

int Hand::lowestInHand()
{
	return (int) (hand.front())->getNumRepresentation();
}

Hand::~Hand() 
{
	for (size_t i = 0; i < hand.size(); i++)
	{
		delete hand[i];
	}
}

vector<Card *> Hand::cloneHand() const
{
	vector<Card*> returnHand;
	for (size_t i = 0; i < hand.size(); i++)
	{
		returnHand.push_back(hand[i]->clone());
	}
	return returnHand;
}


Figure Hand::charToFigure(char charToParse) const
{
	if (charToParse == 'J') return Jack;
	if (charToParse == 'Q') return Queen;
	if (charToParse == 'K') return King;
	if (charToParse == 'A') return Ace;

	return Jack;
}