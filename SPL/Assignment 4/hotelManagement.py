### General notes about hotelManagement.py:
# builds the db and initializes tables from config file (its path will be given as an arg)
# if cronhoteldb.db does not exist, create it, parse the config file and store the data in the db
# otherwise (cronhoteldb.db does exist), exit

import sys
import sqlite3
import os


def main(args):
    if not (os.path.isfile('cronhoteldb.db')):  # if cronhoteldb.db exists, exit
        dbCon = sqlite3.connect('cronhoteldb.db')
        dbCon.text_factory = bytes
        taskId = 0
        with dbCon:
            cursor = dbCon.cursor()
            cursor.execute('CREATE TABLE TaskTimes('
                           'TaskId INTEGER NOT NULL, '
                           'DoEvery INTEGER NOT NULL, '
                           'NumTimes INTEGER NOT NULL, '
                           'PRIMARY KEY(TaskId))')
            cursor.execute('CREATE TABLE Tasks'
                           '(TaskId INTEGER NOT NULL, '
                           'TaskName TEXT NOT NULL, '
                           'Parameter INTEGER, '
                           'FOREIGN KEY(TaskId) REFERENCES TaskTimes(TaskId))')
            cursor.execute('CREATE TABLE Rooms('
                           'RoomNumber INTEGER NOT NULL, '
                           'PRIMARY KEY(RoomNumber))')
            cursor.execute('CREATE TABLE Residents('
                           'RoomNumber INTEGER NOT NULL, '
                           'FirstName TEXT NOT NULL, '
                           'LastName TEXT NOT NULL, '
                           'FOREIGN KEY(RoomNumber) REFERENCES Rooms(RoomNumber))')
            inputFileName = args[1]
            with open(inputFileName) as inputFile:
                for line in inputFile:
                    if line[-1] == '\n':
                        line = line[0:-1]
                    currEntry = line.split(',') # Creates array of strings
                    if currEntry[0] == 'room':
                        if len(currEntry) == 2:
                            cursor.execute('INSERT INTO Rooms VALUES(?)',(int(currEntry[1]),))
                        elif len(currEntry) == 4:
                            cursor.execute('INSERT INTO Rooms VALUES(?)', (int(currEntry[1]),))
                            cursor.execute('INSERT INTO Residents VALUES(?,?,?)',
                                           (int(currEntry[1]), currEntry[2], currEntry[3],))
                        else:
                            print("Illegal entry.")
                    elif currEntry[0] == 'clean':
                        if len(currEntry) == 3:
                            cursor.execute('INSERT INTO Tasks VALUES(?,?,?)',
                                           (taskId, currEntry[0], 0,))
                            cursor.execute('INSERT INTO TaskTimes VALUES(?,?,?)',
                                           (taskId, int(currEntry[1]), int(currEntry[2]),))
                            taskId += 1
                        else:
                            print("Illegal entry.")
                    elif currEntry[0] == 'breakfast':
                        if len(currEntry) == 4:
                            cursor.execute('INSERT INTO Tasks VALUES(?,?,?)',
                                           (taskId, currEntry[0], int(currEntry[2]),))
                            cursor.execute('INSERT INTO TaskTimes VALUES(?,?,?)',
                                           (taskId, int(currEntry[1]), int(currEntry[3]),))
                            taskId += 1
                        else:
                            print("Illegal entry.")
                    elif currEntry[0] == 'wakeup':
                        if len(currEntry) == 4:
                            cursor.execute('INSERT INTO Tasks VALUES(?,?,?)',
                                           (taskId, currEntry[0], int(currEntry[2]),))
                            cursor.execute('INSERT INTO TaskTimes VALUES(?,?,?)',
                                           (taskId, int(currEntry[1]), int(currEntry[3]),))
                            taskId += 1
                        else:
                            print("Illegal entry.")
                    else:
                        print("Illegal entry.")
        dbCon.commit()
        dbCon.close()

if __name__ == '__main__':
    main(sys.argv)