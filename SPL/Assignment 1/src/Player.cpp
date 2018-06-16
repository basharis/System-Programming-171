#include "Player.h"

Player::Player(string playerName): Hand(), name(playerName) {}
Player::Player(string playerName, vector<Card*> newHand) : Hand(newHand), name(playerName) {}

string Player::getName() const {return name;}

void Player::ask(Player* playerToAsk, int cardToAsk, Deck* gameDeck)
{
	if (verbal == 1) { std::cout << this->getName() << " asked " << playerToAsk->getName() << " for the value " << Card::numRepToStr(cardToAsk) << std::endl; }
	int* cardsToMove = playerToAsk->Search(cardToAsk);
	int numCardsToMove = 0;
	int index = cardsToMove[0];
	while (numCardsToMove < 3 && cardsToMove[numCardsToMove] > -1)
	{
		Card& toMove = *playerToAsk->getCardByIndex(index);
		bool flag = this->addCard(toMove);
		playerToAsk->removeCard(toMove);
		if (!flag)
		{
			delete &toMove;
		}
		numCardsToMove++;
	}
	if (numCardsToMove == 0)
	{
		if (gameDeck->getNumberOfCards() > 0)
		{
			Card* toAdd = gameDeck->fetchCard();
			if (this->addCard(*toAdd) == false)
				delete toAdd;
		}
	}
	else if (playerToAsk->getNumberOfCards() > 0)
	{
		for (int j = 0; j < numCardsToMove; j++)
		{
			if (gameDeck->getNumberOfCards() > 0)
			{
				Card* toAdd = gameDeck->fetchCard();
				if (playerToAsk->addCard(*toAdd) == false)
					delete toAdd;
			}
		}
	}
	delete[] cardsToMove;
}

const int Player::whoHasMostCards(const vector<Player*>& playersVec, int myPosition)
{
	int tempMax = 0;
	int returnIndex;
	for (int i = 0; i<(int) playersVec.size(); i++)
	{
		if (i != myPosition)
		{
			int curr = playersVec[i]->getNumberOfCards();
			if (curr >= tempMax)
			{
				tempMax = curr;
				returnIndex = i;
			}
		}
	}
	return returnIndex;
}

Player::~Player() {}



/* PLAYER TYPE 1 */
PlayerType1::PlayerType1(string name, int position) : Player(name) , playerToAsk(), myPosition(position) {}
PlayerType1::PlayerType1(string name, int position, vector<Card*> newHand) : Player(name, newHand), playerToAsk(), myPosition(position) {}

int PlayerType1::haveMost()
{
	return mostInHand();
}
void PlayerType1::playTurn(const vector<Player*>& playersVec, Deck* deck)
{
	int playerToAsk = whoHasMostCards(playersVec, myPosition);
	int cardToAsk = haveMost();
	ask(playersVec[playerToAsk], cardToAsk, deck);
}
PlayerType1* PlayerType1::clone() const
{
	return (new PlayerType1(this->getName(), this->myPosition, this->cloneHand()));
}
PlayerType1::~PlayerType1() {}




/* PLAYER TYPE 2 */
PlayerType2::PlayerType2(string name, int position) : Player(name), playerToAsk(), myPosition(position) {}
PlayerType2::PlayerType2(string name, int position, vector<Card*> newHand) : Player(name, newHand), playerToAsk(), myPosition(position) {}

int PlayerType2::haveLeast()
{
	return leastInHand();
}
void PlayerType2::playTurn(const vector<Player*>& playersVec, Deck* deck) 
{
	int playerToAsk = whoHasMostCards(playersVec, myPosition);
	int cardToAsk = haveLeast();
	ask(playersVec[playerToAsk], cardToAsk, deck);
}

PlayerType2* PlayerType2::clone() const 
{
	return (new PlayerType2(this->getName(), this->myPosition, this->cloneHand()));
}

PlayerType2::~PlayerType2() {}





/* PLAYER TYPE 3 */
PlayerType3::PlayerType3(string name, int position) : Player(name), playerToAsk(-1), myPoisition(position) {}
PlayerType3::PlayerType3(string name, int position, vector<Card*> newHand) : Player(name, newHand), playerToAsk(-1), myPoisition(position) {}


int PlayerType3::highestValue()
{
	return highestInHand();
}
void PlayerType3::playTurn(const vector<Player*>& playersVec, Deck* deck)
{
	playerToAsk = (playerToAsk+1)%(playersVec.size());
	if (playerToAsk == myPoisition)
		playerToAsk = (playerToAsk + 1) % playersVec.size();
	int cardToAsk = highestValue();
	ask(playersVec[playerToAsk], cardToAsk, deck);
}
PlayerType3* PlayerType3::clone() const 
{
	return (new PlayerType3(this->getName(), this->myPoisition, this->cloneHand()));
}

PlayerType3::~PlayerType3() {}





/* PLAYER TYPE 4 */
PlayerType4::PlayerType4(string name, int position) : Player(name), playerToAsk(-1), myPoisition(position) {}
PlayerType4::PlayerType4(string name, int position, vector<Card*> newHand) : Player(name, newHand), playerToAsk(-1), myPoisition(position) {}


int PlayerType4::lowestValue()
{
	return lowestInHand();
}
void PlayerType4::playTurn(const vector<Player*>& playersVec, Deck* deck)
{
	playerToAsk = (playerToAsk + 1) % (playersVec.size());
	if (playerToAsk == myPoisition)
		playerToAsk = (playerToAsk + 1) % (playersVec.size());
	int cardToAsk = lowestValue();
	ask(playersVec[playerToAsk], cardToAsk, deck);
}
PlayerType4* PlayerType4::clone() const
{
	return (new PlayerType4(this->getName(), this->myPoisition, this->cloneHand()));
}

PlayerType4::~PlayerType4() {}


