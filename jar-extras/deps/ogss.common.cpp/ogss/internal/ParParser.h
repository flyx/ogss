//
// Created by Timm Felden on 16.05.19.
//

#ifndef OGSS_TEST_CPP_PARPARSER_H
#define OGSS_TEST_CPP_PARPARSER_H


#include "Parser.h"
#include "../concurrent/Semaphore.h"
#include "../concurrent/Pool.h"

namespace ogss {
    namespace internal {

        /**
         * A parallel .sg-file parser.
         *
         * @author Timm Felden
         */
        class ParParser final : public Parser {
            concurrent::Semaphore barrier;

            // jobs is a field as we need it for await
            std::vector<concurrent::Job *> jobs;

            ParParser(const std::string &path, streams::FileInputStream *in, const PoolBuilder &pb);

            // await parellel read jobs
            ~ParParser() final;

            void typeBlock() final;

            void processData() final;

            struct AllocateInstances final : public concurrent::Job {
                AbstractPool *const p;
                concurrent::Semaphore *const barrier;

                AllocateInstances(AbstractPool *p, concurrent::Semaphore *barrier)
                        : p(p), barrier(barrier) {}

                void run() final {
                    concurrent::Semaphore::ScopedPermit release(barrier);
                    p->allocateInstances();
                }
            };

            struct AllocateHull final : public concurrent::Job {
                HullType *const p;
                const int count;
                streams::MappedInStream *const map;
                ParParser *const self;

                AllocateHull(HullType *p, int count, streams::MappedInStream *map, ParParser *self)
                        : p(p), count(count), map(map), self(self) {}

                void run() final;
            };

            friend struct StateInitializer;
        };
    }
}


#endif //SKILL_CPP_TESTSUITE_PARPARSER_H