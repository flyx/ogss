//
// Created by Timm Felden on 20.05.19.
//

#include "Pool.h"
#include "../utils.h"

using namespace ogss::concurrent;

Pool::Pool() :
        workerCount(std::thread::hardware_concurrency()), workers(new std::thread *[workerCount]),
        mx(), cv(), jobs(), errors(), shutdown(false) {
    for (size_t i = 0; i < workerCount; i++) {
        workers[i] = new std::thread([](Pool *const p) {
            while (true) {
                Job *next;
                // we have just been created or completed a job, so get the next job
                {
                    std::unique_lock<std::mutex> lock(p->mx);
                    // on while instead of if: threads can be woken up accidentally
                    // @see https://en.wikipedia.org/wiki/Spurious_wakeup
                    while (!p->shutdown & p->jobs.empty())
                        p->cv.wait(lock);

                    // prefer shutdown over the next job
                    if (p->shutdown)
                        return;

                    // no shutdown, so there must be a job
                    next = p->jobs.front();
                    p->jobs.pop_front();
                }

                // try execute
                try {
                    next->run();
                } catch (std::exception &e) {
                    p->errors.push_back(e);
                } catch (...) {
                    p->errors.push_back(std::logic_error("run threw a non std::exception"));
                }

                // try delete
                try {
                    delete next;
                } catch (std::exception &e) {
                    p->errors.push_back(e);
                } catch (...) {
                    p->errors.push_back(std::logic_error("delete threw a non std::exception"));
                }
            }
        }, this);
    }
}

Pool::~Pool() {
    {
        std::lock_guard<std::mutex> lock(mx);
        shutdown = true;
    }
    cv.notify_all();

    // wait for workers to terminate
    for (size_t i = 0; i < workerCount; i++)
        workers[i]->join();

    delete[] workers;

    // delete remaining jobs
    for (Job *j : jobs)
        delete j;

}

std::exception Pool::popError() {
    std::lock_guard<std::mutex> lock(mx);
    std::exception r = errors.back();
    errors.pop_back();
    return r;
}
