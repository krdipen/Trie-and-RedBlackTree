package PriorityQueue;

public interface PriorityQueueInterface<T extends Comparable> {

    void insert(T element);
    T extractMax();

}
