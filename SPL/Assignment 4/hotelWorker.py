### General notes about hotelWorker.py:
# should have a connection to the db to fetch first and last name for the room number (parameter), and which rooms are empty
# if taskName = 'wakeup' then parameter = room number, print "[firstName] [lastName] received a wakeup call at [time]"
# if taskName = 'breakfast' then parameter = room number, print "[firstName] [lastName] has been served breakfast at [time]"
# if taskName = 'clean' then parameter is irrelevant (0), print "Rooms [roomNum1, roomNum2, ... , roomNumN] were cleaned at [time]" (in ascending order)
# doHotelTask() returns the time of execution $t_i$ (should be time $t_i-1$ + interval (DoEvery from TaskTimes))
# neglect up to 1 sec up or down from the expected #t_i$
# use str(time.time()) to print the time

import sys
import sqlite3
import os
import time

def doHotelTask(taskname, parameter):
    dbCon = sqlite3.connect('cronhoteldb.db')
    dbCon.text_factory = bytes

    cursor = dbCon.cursor()
    if taskname == 'breakfast':
        cursor.execute('SELECT * FROM Residents WHERE RoomNumber = (?)', (int(parameter),))
        roomInfo = cursor.fetchone()
        roomNumber = roomInfo[0]
        firstName = roomInfo[1]
        # print roomInfo
        lastName = roomInfo[2] 
        taskTime = time.time()
        print('{} {} in room {} has been served breakfast at {}'.format(firstName,lastName,str(roomNumber),taskTime))
        return taskTime
    elif taskname == 'wakeup':
        cursor.execute('SELECT * FROM Residents WHERE RoomNumber = (?)', (int(parameter),))
        roomInfo = cursor.fetchone()
        roomNumber = roomInfo[0]
        firstName = roomInfo[1]
        lastName = roomInfo[2]
        taskTime = time.time()
        print('{} {} in room {} received a wakeup call at {}'.format(firstName,lastName,str(roomNumber),taskTime))
        return taskTime
    else:  # clean
        cursor.execute('SELECT RoomNumber '
                       'FROM Rooms '
                       'WHERE RoomNumber NOT IN '
                       '(SELECT RoomNumber FROM Residents) '
                       'ORDER BY RoomNumber ASC')
        roomsToClean = cursor.fetchall()
        roomsToCleanString = ''
        for room in roomsToClean:
            roomsToCleanString +=', {}'.format(str(room[0]))
        roomsToCleanString = roomsToCleanString[2:]
        taskTime = time.time()
        print('Rooms {} were cleaned at {}'.format(roomsToCleanString,taskTime))
        return taskTime
		
		
		
		
		
		
		