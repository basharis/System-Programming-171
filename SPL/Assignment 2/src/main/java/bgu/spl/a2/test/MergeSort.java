/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

	private final int[] array;

	public MergeSort(int[] array) {
		this.array = array;
	}

	@Override
	protected void start() {
		if (array.length == 1){
			complete(array);
		}
		else{
			int halfArraySize = array.length/2;
			if (array.length % 2 == 1)
				halfArraySize++;
			int[] firstHalf = new int[halfArraySize];
			int[] secondHalf = new int[array.length/2];
			System.arraycopy(array, 0, firstHalf, 0, halfArraySize);
			System.arraycopy(array, halfArraySize, secondHalf, 0, array.length/2);
			MergeSort sortFirstHalf = new MergeSort(firstHalf);
			MergeSort sortSecondHalf = new MergeSort(secondHalf);
			ConcurrentLinkedQueue<MergeSort> subTasks = new ConcurrentLinkedQueue<>();
			subTasks.add(sortFirstHalf);
			subTasks.add(sortSecondHalf);
			whenResolved(subTasks, () -> { mergeSortedArrays(subTasks); });
			spawn(sortFirstHalf);
			spawn(sortSecondHalf);

		}

	}

	private void mergeSortedArrays(ConcurrentLinkedQueue<MergeSort> tasksToMerge) {

		int[] sortedArray = new int[array.length];
		int posFirstHalf=0,posSecondHalf=0,posSortedArr=0;
		int[] firstHalf = tasksToMerge.poll().getResult().get();
		int[] secondHalf = tasksToMerge.poll().getResult().get();
		while (posFirstHalf < firstHalf.length && posSecondHalf < secondHalf.length){
			if (firstHalf[posFirstHalf] >= secondHalf[posSecondHalf]){
				sortedArray[posSortedArr] = secondHalf[posSecondHalf];
				posSecondHalf++;
				posSortedArr++;
			}
			else{
				sortedArray[posSortedArr] = firstHalf[posFirstHalf];
				posFirstHalf++;
				posSortedArr++;
			}
		}
		while (posFirstHalf < firstHalf.length){
			sortedArray[posSortedArr] = firstHalf[posFirstHalf];
			posFirstHalf++;
			posSortedArr++;
		}
		while (posSecondHalf < secondHalf.length){
			sortedArray[posSortedArr] = secondHalf[posSecondHalf];
			posSecondHalf++;
			posSortedArr++;
		}
		complete(sortedArray);

	}

	public static void main(String[] args) throws InterruptedException {
		WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
		int n = 78554; //you may check on different number of elements if you like
		int[] array = new Random().ints(n).toArray();

		MergeSort task = new MergeSort(array);

		CountDownLatch l = new CountDownLatch(1);
		pool.start();
		pool.submit(task);
		task.getResult().whenResolved(() -> {
			//warning - a large print!! - you can remove this line if you wish
			System.out.println(Arrays.toString(task.getResult().get()));
			l.countDown();
		});

		l.await();
		pool.shutdown();
	}

}
