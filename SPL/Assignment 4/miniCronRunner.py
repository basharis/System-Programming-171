### General notes about miniCronRunner.py:
# should actually run the tasks using hotelWorker's doHotelTask(taskName, roomNumber)
# it can SELECT TaskTimes table, SELECT Tasks table and UPDATE NumTimes of TaskTimes
# will run in loop until cronhoteldb.db does not exist, or there are no tasks in TaskTimes that has NumTimes > 0
# every time it does a task, it will decrease its NumTimes by 1
# first iteration - do (doHotelTask(taskName, roomNumber)) all entries
# every iteration after - query TaskTimes and see if it still needs doing + its time has come
# we assume TaskTimes > 0 at start and DoEvery > 1 sec
# also assume that input is valid (no unexpected entries)

import sys
import sqlite3
import os
import time
from hotelWorker import doHotelTask

def main(args):
    if (os.path.isfile('cronhoteldb.db')):
        dbCon = sqlite3.connect('cronhoteldb.db')
        dbCon.text_factory = bytes

        # Run for the first time
        lastExecDict = {} # TaskId : Time of last execution
        overallNumOfTasks = 0
        with dbCon:
            cursor = dbCon.cursor()
            cursor.execute('SELECT * FROM Tasks')
            tasksList = cursor.fetchall()
            for task in tasksList:
                taskId = task[0]
                taskName = task[1]
                taskParam = task[2]
                cursor.execute('SELECT NumTimes FROM TaskTimes WHERE TaskId = (?)',(taskId,))
                numTimesOfTask = cursor.fetchone()[0]
                if (numTimesOfTask > 0):
                    cursor.execute('UPDATE TaskTimes SET NumTimes = {} WHERE TaskId = {}'.format(numTimesOfTask - 1, taskId))
                    overallNumOfTasks += numTimesOfTask - 1
                    lastExecDict[taskId] = doHotelTask(taskName, taskParam)
            # Next iterations
            while (os.path.isfile('cronhoteldb.db')) & (overallNumOfTasks > 0):
                cursor.execute('SELECT * FROM Tasks')
                tasksList = cursor.fetchall()
                for task in tasksList:
                    taskId = task[0]
                    taskName = task[1]
                    taskParam = task[2]
                    cursor.execute('SELECT DoEvery, NumTimes FROM TaskTimes WHERE TaskId = (?)', (taskId,))
                    taskInfo = cursor.fetchone()
                    doEveryOfTask = taskInfo[0]
                    numTimesOfTask = taskInfo[1]
                    if (numTimesOfTask > 0) & (time.time() >= lastExecDict[taskId] + doEveryOfTask):
                        lastExecDict[taskId] = doHotelTask(taskName, taskParam)
                        cursor.execute('UPDATE TaskTimes SET NumTimes = {} WHERE TaskId = {}'.format(numTimesOfTask - 1, taskId))
                        overallNumOfTasks -= 1

        dbCon.commit()
        dbCon.close()

if __name__ == '__main__':
    main(sys.argv)