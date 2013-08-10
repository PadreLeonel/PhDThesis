#include "configure.h"
#include "diagnosis/structs/count_spectra.h"
#include "diagnosis/structs/ambiguity_groups.h"
#include "diagnosis/algorithms/barinel.h"
#include "mpi.h"
#include "opt.h"
#include "utils/time.h"


#include <list>
#include <mpi.h>

using namespace diagnosis;
using namespace diagnosis::structs;


int main (int argc, char ** argv) {
    t_mhs_options options(argv[0]);
    t_ambiguity_groups ambiguity_groups;


    if (options.configure(argc, argv))
        return 1;

    t_count_spectra spectra;
    structs::t_trie D;
    algorithms::t_mhs mhs(options.mhs);

    /* Initialize MPI */
    MPI_Init(NULL, NULL);

    /* Find out my identity in the default communicator */
    int ntasks, rank;
    MPI_Comm_size(MPI_COMM_WORLD, &ntasks);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    t_stats stats;

    if (rank == 0)
        options.debug() << options << std::endl;

    spectra.read(options.input(), options.has_confidence);

    if (!options.input().good()) {
        std::cerr << "Problem reading spectra" << std::endl;
        return 1;
    }

    if (!spectra.is_valid()) {
        std::cerr << "Invalid spectra (some failing transactions do not activate any components)" << std::endl;
        return 1;
    }

    if (options.print_spectra)
        options.output() << spectra;

    if (options.ambiguity_groups) {
        ambiguity_groups = t_ambiguity_groups(spectra);

        if (options.print_spectra)
            spectra.write(options.output(), &ambiguity_groups.filter()) << std::endl;

        options.output() << ambiguity_groups << std::endl;
    }

    if (ntasks > 1)
        mhs2_heuristic_setup(mhs, options.mpi_level, options.mpi_stride);

    mhs2_map(mhs, spectra, D, stats, &ambiguity_groups.filter());
    options.debug() << "MHS2_map: Ended" << std::endl;

    if (ntasks > 1) {
        mhs2_reduce(D, options.mpi_hierarchical, options.mpi_buffer, stats);
        options.debug() << "MHS2_reduce: Ended" << std::endl;
    }

    mhs2_collect_stats(options.debug(), D, stats);

    if (rank == 0) {
        if (options.fuzzinel) {
            // Fuzzinel stuff
            typedef diagnosis::t_rank_element<const t_candidate *, t_probability_mp> t_rank_element;
            typedef std::vector<t_rank_element> t_rank;
            diagnosis::algorithms::t_barinel barinel;
            diagnosis::t_probability_mp ret;
            diagnosis::t_probability_mp total_ret(0);
            t_rank probs;

            diagnosis::structs::t_trie::iterator it = D.begin();

            while (it != D.end()) {
                barinel.calculate(spectra, *it, ret);
                options.debug() << "Fuzzinel: Ended for candidate (" << * it << ") with score " << ret << std::endl;
                probs.push_back(t_rank_element(&(*it), ret));
                total_ret += ret;
                it++;
            }

            sort(probs.begin(), probs.end());

            t_rank::iterator it_prob = probs.begin();

            while (it_prob != probs.end()) {
                options.output() << (-it_prob->get_score() / total_ret) << ": " << *it_prob->get_element() << std::endl;
                it_prob++;
            }
        }
        else {
            options.output() << D;
        }
    }

    /* Shut down MPI */
    MPI_Finalize();

    return 0;
}