#include "Card.h"

/* CARD */

Card::Card() : shape() {}
Card::Card(Shape otherShape) : shape(otherShape) {}
string Card::getShape() {
	string charValue;
	switch (shape) {
		case 0: charValue = "C"; break;
		case 1: charValue = "D"; break;
		case 2: charValue = "H"; break;
		case 3: charValue = "S"; break;
	}
	return charValue;
}
Shape Card::getShapeEnum() const {
	return shape;

}

string Card::numRepToStr(int numRep)
{
	string returnValue;
	if (numRep <= _N)
		return to_string(numRep);
	else
	{
		if (numRep == _N + 1) { return "J"; }
		if (numRep == _N + 2) { return "Q"; }
		if (numRep == _N + 3) { return "K"; }
		if (numRep == _N + 4) { return "A"; }
	}
	return "INVALID NUMREP";
}

Card::~Card() {}

/* FIGURE CARD */

FigureCard::FigureCard() : figure() {}
FigureCard::FigureCard(Figure figure, Shape shape) : Card(shape), figure(figure) {}
FigureCard::~FigureCard() {}
string FigureCard::getFigure() {
	string charValue;
	switch (figure) {
		case 0: charValue = "J"; break;
		case 1: charValue = "Q"; break;
		case 2: charValue = "K"; break;
		case 3: charValue = "A"; break;
	}
	return charValue;
}
string FigureCard::toString() {
	string returnValue = getFigure() + getShape();
	return returnValue;
}
double FigureCard::getNumRepresentation() {
	double num;
	switch (figure) {
		case 0: num = _N + 1; break;
		case 1: num = _N + 2; break;
		case 2: num = _N + 3; break;
		case 3: num = _N + 4; break;
	}
	switch (getShapeEnum()) {
		case 0: num += 0.1; break;
		case 1: num += 0.2; break;
		case 2: num += 0.3; break;
		case 3: num += 0.4; break;
	}
	return num;
}

FigureCard* FigureCard::clone() const
{
	FigureCard* fc = new FigureCard(this->figure, this->getShapeEnum());
	return fc;
}


/* NUMERIC CARD */

NumericCard::NumericCard() : number() {}
NumericCard::NumericCard(int number, Shape shape) : Card(shape) , number(number) {}
string NumericCard::getNumber() {
	return std::to_string(number);
}
int NumericCard::getNumberInt() {
	return number;
}
string NumericCard::toString() {
	string returnValue = getNumber() + getShape();
	return returnValue;
}
double NumericCard::getNumRepresentation() {
	double num = getNumberInt();
	switch (getShapeEnum()) {
		case 0: num += 0.1; break;
		case 1: num += 0.2; break;
		case 2: num += 0.3; break;
		case 3: num += 0.4; break;
	}
	return num;
}

NumericCard* NumericCard::clone() const
{
	NumericCard* nc = new NumericCard(this->number, this->getShapeEnum());
	return nc;
}

NumericCard::~NumericCard() {}
