#ifndef SAFE_QUEUE
#define SAFE_QUEUE

#include <queue>
#include <mutex>
#include <condition_variable>

// A threadsafe-queue.
template <class T>
class SafeQueue
{
public:
  SafeQueue(void){}
  ~SafeQueue(void){}

  // Add an element to the queue.
  void enqueue(T t) {}

  // Get the "front"-element.
  // If the queue is empty, wait till a element is avaiable.
  T dequeue(void){}

private:
  std::queue<T> q;
  mutable std::mutex m;
  std::condition_variable c;
};
#endif