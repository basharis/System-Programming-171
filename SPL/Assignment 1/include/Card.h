#ifndef CARD_H_
#define CARD_H_

#include <iostream>
#include <string>

#include "Global.h"

using namespace std;

enum Shape {
	Club,
	Diamond,
	Heart,
	Spade
};

enum Figure {
	Jack,
	Queen,
	King,
	Ace
};

class Card {
private:
  Shape shape;
public:
  virtual string toString() = 0; //Returns the string representation of the card "<value><shape>" exp: "12S" or "QD"
  Card();
  Card(Shape);
  virtual ~Card();
  string getShape();
  Shape getShapeEnum() const;
  static string numRepToStr(int);
  virtual double getNumRepresentation() = 0;
  virtual Card* clone() const = 0;
};

class FigureCard : public Card {
private:
	Figure figure;
public:
	FigureCard();
	FigureCard(Figure, Shape);
	virtual ~FigureCard();
	string getFigure();
	virtual string toString() override;
	virtual double getNumRepresentation() override;
	virtual FigureCard* clone() const override;
};

class NumericCard : public Card {
private:
	int number;
public:
	NumericCard();
	NumericCard(int, Shape);
	string getNumber();
	int getNumberInt();
	virtual string toString() override;
	virtual double getNumRepresentation() override;
	virtual ~NumericCard();
	virtual NumericCard* clone() const override;

	
};

#endif
